package com.politrons.grpc

import com.politrons.grpc.PrimeNumberServiceGrpc.PrimeNumberServiceImplBase
import io.grpc.ServerBuilder
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, Runtime, ZIO, ZLayer, ZManaged}

object PrimerNumberServer extends App {

  private val logger: Logger = LoggerFactory.getLogger("PrimerNumberServer")

  private val primeNumberServerProgram = createPrimeNumberServer()
  private val service: PrimeNumberServiceImplBase = new PrimeNumberServiceImpl()
  Runtime.global.unsafeRun(primeNumberServerProgram.provideLayer(ZLayer.succeed(service)))

  /**
   * Program to create and run the gRPC server.
   * It receives as dependency in the program the implementation of the gRPC service [PrimeNumberServiceImplBase]
   * Once the server is running, the we can start the communication between client/server though that service.
   */
  def createPrimeNumberServer(port: Int = 9995): ZIO[Has[PrimeNumberServiceImplBase], Throwable, Unit] = {
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
