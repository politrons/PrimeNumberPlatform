package com.politrons.grpc

import io.grpc.stub.StreamObserver

class PrimeNumberServiceImpl extends PrimeNumberServiceGrpc.PrimeNumberServiceImplBase {

  override def findPrimeNumbers(responseObserver: StreamObserver[PrimeNumberResponse]): StreamObserver[PrimeNumberRequest] = {
    new StreamObserver[PrimeNumberRequest]() {
      override def onNext(primeNumber: PrimeNumberRequest): Unit = {

        sieveOfEratosthenes(Stream.from(2))
          .takeWhile(prime => prime <= primeNumber.getAttr.toInt)
          .foreach { prime =>
            val response: PrimeNumberResponse =
              PrimeNumberResponse.newBuilder.setValue(prime.toString).build
            responseObserver.onNext(response)
          }
      }

      override def onError(t: Throwable): Unit = {
        responseObserver.onError(t)
      }

      override def onCompleted(): Unit = {
        responseObserver.onCompleted()
      }
    }
  }

  /**
   * Sieve of Eratosthenes
   * ---------------------
   * Eratosthenes of Cyrene was a Greek mathematician, who discovered an amazing
   * algorithm to find prime numbers.
   *
   * #:: create a Stream of an element x followed by a lazy Stream xs
   * In each iteration we filter by get the head element of the Stream and we
   * div between all current members of the stream.
   * Leaving in the stream only the prime numbers
   */
  def sieveOfEratosthenes(stream: Stream[Int]): Stream[Int] = {
    stream.head #:: sieveOfEratosthenes(
      stream.tail.filter(x => x % stream.head != 0)
    )
  }
}