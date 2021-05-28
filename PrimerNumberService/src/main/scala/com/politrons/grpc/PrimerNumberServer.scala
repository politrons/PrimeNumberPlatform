package com.politrons.grpc

import io.grpc.{Server, ServerBuilder}


object PrimerNumberServer extends App {

  val port = 9995
  val server: Server =
    ServerBuilder.forPort(port)
      .addService(new PrimeNumberServiceImpl()).asInstanceOf[ServerBuilder[_]]
      .build()
      .start()

  server.awaitTermination()

}
