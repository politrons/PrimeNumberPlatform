package com.politrons.mocks

import com.politrons.grpc.{PrimeNumberRequest, PrimeNumberResponse, PrimeNumberServiceGrpc}
import io.grpc.stub.StreamObserver
import io.grpc.{Server, ServerBuilder}

/**
 * Mock server to emulate the real one of PrimeNumberService module
 * The only thing it will do is just make an echo of whatever we send to him.
 */
object PrimerNumberServerMock {

  var server: Server = _

  def start(port: Int = 9995) {
    server =
      ServerBuilder.forPort(port)
        .addService(new PrimeNumberServiceGrpc.PrimeNumberServiceImplBase {
          override def findPrimeNumbers(responseObserver: StreamObserver[PrimeNumberResponse]): StreamObserver[PrimeNumberRequest] = {
            new StreamObserver[PrimeNumberRequest]() {
              override def onNext(value: PrimeNumberRequest): Unit = {
                val response: PrimeNumberResponse = PrimeNumberResponse.newBuilder.setValue(value.getAttr).build
                responseObserver.onNext(response)
              }

              override def onError(t: Throwable): Unit = {
                responseObserver.onError(t)
              }

              override def onCompleted(): Unit = {
                responseObserver.onCompleted()
              }
            }
          }
        }).asInstanceOf[ServerBuilder[_]]
        .build()
        .start()
  }

  def stop(): Unit ={
    server.shutdownNow()
  }

}
