package com.politrons.api

import com.politrons.grpc.PrimerNumberClient
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import zio.{Has, Runtime, ZIO, ZLayer}

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

  //TODO:Pass PrimerNumberClient as a dependency to the ProxyServer
  private val primeNumberClient = PrimerNumberClient()

  def start(port: Int) {
    Http.server
      .withStreaming(enabled = true)
      .serve(s"0.0.0.0:$port", service)
  }

  val service: Service[Request, Response] = (req: http.Request) => {
    req.path match {
      case "/prime/:number" =>
        val primeNumber = req.getParam("number")
        val buf = Buf.Utf8(primeNumber)
        //TODO:Change Scala future for ZIO Fiber
        scala.concurrent.Future {
          //TODO:Add logic of validation
          val primeNumberProgram: ZIO[Has[Reader.Writable], Throwable, Unit] =
            primeNumberClient.findPrimeNumbers(primeNumber)
          Runtime.global.unsafeRun(primeNumberProgram.provideLayer(ZLayer.succeed(writable)))
        }
        Future.value(Response(req.version, Status.Ok, writable))
    }
  }


}
