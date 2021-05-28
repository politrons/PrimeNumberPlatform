package com.politrons.grpc

import io.grpc.stub.StreamObserver

import scala.concurrent.Future

class PrimeNumberServiceImpl extends PrimeNumberServiceGrpc.PrimeNumberServiceImplBase {

  override def findPrimeNumbers(responseObserver: StreamObserver[PrimeNumberResponse]): StreamObserver[PrimeNumberRequest] = {
    new StreamObserver[PrimeNumberRequest]() {
      override def onNext(value: PrimeNumberRequest): Unit = {
        //TODO:Implement Prime number creation logic
        val response: PrimeNumberResponse = PrimeNumberResponse.newBuilder.setValue(value.getAttr).build
        Future {
          while (true) {
            System.out.println("Value from client: " + value.getAttr)
            responseObserver.onNext(response)
            Thread.sleep(1000)
          }

        }(scala.concurrent.ExecutionContext.global)
      }

      override def onError(t: Throwable): Unit = {
        responseObserver.onError(t)
      }

      override def onCompleted(): Unit = {
        responseObserver.onCompleted()
      }
    }
  }


}