package com.politrons.api

import com.politrons.grpc.{PrimeNumberClient, PrimerNumberClientImpl}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service, http}
import com.twitter.io.Reader
import com.twitter.util.{Await, Future}
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, Runtime, Task, ZIO, ZLayer, ZManaged}

import scala.concurrent.ExecutionContextExecutor

/**
 * Http server based in [Finagle](https://twitter.github.io/finagle/) toolkit,
 * an extensible RPC system for the JVM from Twitter company.
 * It provide not only the possibility to use RPC but also HTTP 2.0 using streaming
 * between client and server, having a [Reader] for the client side, and a [Writable]
 * in the server side to keep communication open between client-server
 */
object ProxyServer {

  private val logger: Logger = LoggerFactory.getLogger("ProxyServer")

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  private val writable: Reader.Writable = Reader.writable()

  /**
   * Entry point of the program to be started.
   */
  def main(args: Array[String]): Unit = {
    val port = 9994
    val serverProgram = start(port)
    val primeNumberClient: PrimeNumberClient = PrimerNumberClientImpl()
    Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(primeNumberClient)))
  }

  /**
   * ZIO program to start up the [Finagle] server in a specific port.
   * We pass a Dependency to the program the gRPC client [PrimeNumberClient],
   * to communicate with other service.
   */
  def start(port: Int): ZIO[Has[PrimeNumberClient], Throwable, Unit] = {
    (for {
      primeNumberClient <- ZManaged.service[PrimeNumberClient].useNow
      service <- createService(primeNumberClient)
      server <- createServer(port, service)
      _ <- ZIO.effect(Await.ready(server))
    } yield logger.info(s"[ProxyServer] server up and running in port $port")).catchAll { t =>
      logger.error(s"[ProxyServer] Error initializing. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.fail(t)
    }
  }

  /**
   * We create a [ListeningServer] where we specify the operator [withStreaming(enabled = true)]
   * Allowing a communication between client-server with an infinite body, using a stream with
   * [Reader-Writable]
   */
  private def createServer(port: Int, service: Service[Request, Response]): Task[ListeningServer] = {
    ZIO.effect {
      Http.server
        .withStreaming(enabled = true)
        .serve(s"0.0.0.0:$port", service)
    }
  }

  /**
   * Here we define the service with the endpoint [/prime/:number] which expect a uri param as prime number limit.
   * Once we receive the request we create a ZIO program that run asynchronously in a Fiber, so there's
   * non blocking request logic.
   */
  def createService(client: PrimeNumberClient): Task[Service[Request, Response]] =
    ZIO.effect {
      (req: http.Request) => {
        req.path match {
          case "/prime/:number" =>
            val dependencies = ZLayer.succeed(req) ++ ZLayer.succeed(writable)
            val primeNumberRequestProgram = createPrimeNumberRequestProgram(client, req)
            Runtime.global.unsafeRun(primeNumberRequestProgram.provideLayer(dependencies))
        }
      }
    }

  /**
   * This ZIO program receive in the evaluation time the dependencies they needs. The [Request] of the communication,
   * and the [Writable]
   * We return a [writable] so then we can continuous sending data.
   */
  private def createPrimeNumberRequestProgram(client: PrimeNumberClient,
                                              req: Request): ZIO[Has[Reader.Writable] with Has[Request], Nothing, Future[Response]] = {
    (for {
      request <- ZManaged.service[Request].useNow
      primeNumber <- ZIO.effect(request.getParam("number"))
      _ <- ZIO.effect(primeNumber.toInt)
      _ <- client.findPrimeNumbers(primeNumber).forkDaemon
    } yield Future.value(Response(req.version, Status.Ok, writable))).catchAll(t => {
      logger.error(s"[ProxyServer] Error in prime number request. Caused by ${ExceptionUtils.getStackTrace(t)}")
      ZIO.succeed(Future.value(Response(req.version, Status.InternalServerError)))
    })
  }
}
