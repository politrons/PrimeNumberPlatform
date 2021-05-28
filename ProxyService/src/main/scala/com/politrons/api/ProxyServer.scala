package com.politrons.api

import com.politrons.grpc.PrimerNumberClient
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Awaitable, Future}
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

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  private val writable: Reader.Writable = Reader.writable()

  def main(args: Array[String]): Unit = {
    val serverProgram = start(9995)
    Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(PrimerNumberClient())))
  }

  def start(port: Int): ZIO[Has[PrimerNumberClient], Throwable, Unit] = {
    (for {
      primeNumberClient <- ZManaged.service[PrimerNumberClient].useNow
      service <- createService(primeNumberClient)
      _ <- createServer(port, service)
    } yield ()).catchAll { t =>
      println(s"[ProxyServer] Error initializing. Caused by $t")
      ZIO.fail(t)
    }
  }

  private def createServer(port: Int, service: Service[Request, Response]): Task[ListeningServer] = {
    ZIO.effect {
      Http.server
        .withStreaming(enabled = true)
        .serve(s"0.0.0.0:$port", service)
    }
  }

  def createService(client: PrimerNumberClient): Task[Service[Request, Response]] =
    ZIO.effect {
      (req: http.Request) => {
        req.path match {
          case "/prime/:number" =>
            val primeNumber = req.getParam("number")
            val buf = Buf.Utf8(primeNumber)
            //TODO:Change Scala future for ZIO Fiber
            scala.concurrent.Future {
              //TODO:Add logic of validation
              //TODO:Pass PrimerNumberClient as a dependency to the ProxyServer
              val primeNumberProgram: ZIO[Has[Reader.Writable], Throwable, Unit] =
              client.findPrimeNumbers(primeNumber)
              Runtime.global.unsafeRun(primeNumberProgram.provideLayer(ZLayer.succeed(writable)))
            }
            Future.value(Response(req.version, Status.Ok, writable))
        }
      }
    }
}
