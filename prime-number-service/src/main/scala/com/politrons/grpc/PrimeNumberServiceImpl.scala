package com.politrons.grpc

import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class PrimeNumberServiceImpl extends PrimeNumberServiceGrpc.PrimeNumberServiceImplBase {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PrimeNumberServiceImpl])

  /**
   * Service implementation of gRPC contract.
   * It receive as input param the StreamObserver[PrimeNumberResponse] of the client has created,
   * in order to communicate obtain communication back to the client.And the StreamObserver[PrimeNumberRequest]
   * as return type to be used by the client to invoke [onNext] for request.
   */
  override def findPrimeNumbers(responseObserver: StreamObserver[PrimeNumberResponse]): StreamObserver[PrimeNumberRequest] = {
    new StreamObserver[PrimeNumberRequest]() {
      override def onNext(primeNumber: PrimeNumberRequest): Unit = {
        logger.debug("[PrimeNumberServiceImpl] Prime number limit received to generate prime numbers")
        sieveOfEratosthenes(Stream.from(2))
          .takeWhile(prime => prime <= primeNumber.getAttr.toInt)
          .foreach { prime =>
            logger.debug(s"[PrimeNumberServiceImpl] Prime number:$prime")
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