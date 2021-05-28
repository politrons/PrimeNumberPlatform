package com.politrons.grpc

import com.politrons.grpc.PrimeNumberServiceGrpc.PrimeNumberServiceImplBase
import io.grpc.ServerBuilder
import zio.{Has, Runtime, ZIO, ZLayer, ZManaged}

object PrimerNumberServer extends App {

  val primeNumberServerProgram = start()
  val service: PrimeNumberServiceImplBase = new PrimeNumberServiceImpl()
  Runtime.global.unsafeRun(primeNumberServerProgram.provideLayer(ZLayer.succeed(service)))

  def start(port: Int = 9995): ZIO[Has[PrimeNumberServiceImplBase], Throwable, Unit] = {
    (for {
      service <- ZManaged.service[PrimeNumberServiceImplBase].useNow
      server <- ZIO.effect {
        ServerBuilder.forPort(port)
          .addService(service).asInstanceOf[ServerBuilder[_]]
          .build()
          .start()
      }
    } yield server.awaitTermination()).catchAll {
      t =>
        println(s"[PrimerNumberServer] Error initializing. Caused by ${t.getCause}")
        ZIO.fail(t)
    }
  }

}
