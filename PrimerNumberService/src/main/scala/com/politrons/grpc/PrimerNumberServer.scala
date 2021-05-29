package com.politrons.grpc

import com.politrons.grpc.PrimeNumberServiceGrpc.PrimeNumberServiceImplBase
import io.grpc.ServerBuilder
import org.apache.logging.log4j.{LogManager, Logger}
import zio.{Has, Runtime, ZIO, ZLayer, ZManaged}

object PrimerNumberServer extends App {

  private val logger: Logger = LogManager.getLogger("PrimerNumberServer")
  private val primeNumberServerProgram = start()
  private val service: PrimeNumberServiceImplBase = new PrimeNumberServiceImpl()
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
        logger.error(s"[PrimerNumberServer] Error initializing. Caused by ${t.getCause}")
        ZIO.fail(t)
    }
  }

}
