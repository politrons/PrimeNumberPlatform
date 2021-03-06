package com.politrons.grpc

import com.twitter.io.{Buf, Reader}
import com.twitter.util.Await
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.{Logger, LoggerFactory}
import zio.{Has, ZIO, ZManaged}

case class PrimerNumberClientImpl() extends PrimeNumberClient {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PrimerNumberClientImpl])

  /**
   * Since I don't have enough time, I don't put properly the config properties in property files.
   * Here we create the [ManagedChannel] which it will be used to establish the communication with the
   * remote server
   */
  private val channel = ManagedChannelBuilder
    .forAddress("localhost", 9995)
    .usePlaintext(true).asInstanceOf[ManagedChannelBuilder[_]]
    .build()

  /**
   * [PrimeNumberServiceStub] it contain the Service API that was described in the protobuf file.
   */
  private val stub: PrimeNumberServiceGrpc.PrimeNumberServiceStub = PrimeNumberServiceGrpc.newStub(channel)

  /**
   * Function that receive the prime number limit to find, and it return a ZIO program to control all
   * possible side-effects.
   * The program it will do the next steps:
   * * The program it will get the [Reader.Writable] as dependency.
   * * Create a request with the prime number to be send to the gRpc server.
   * * Create the StreamObserver passing the [Reader.Writable] to be used internally
   * * run the onNext function of the stream passing the request created previously.
   */
  override def findPrimeNumbers(number: String): ZIO[Has[Reader.Writable], Throwable, Unit] = {
    (for {
      writable <- ZManaged.service[Reader.Writable].useNow
      request <- ZIO.effect(PrimeNumberRequest.newBuilder.setAttr(number).build)
      stream <- ZIO.effect(createStreamObserver(writable))
      _ <- ZIO.effect(stream.onNext(request))
    } yield ()).catchAll(t => {
      logger.error(s"[PrimerNumberClient] Error: Caused by ${ExceptionUtils.getStackTrace(t)} ")
      ZIO.fail(t)
    })
  }

  /**
   * In order to keep open the communication and being able to receive continuously streams with all prime numbers
   * We create the StreamObserver[PrimeNumberResponse] and we pass to the function [findPrimeNumbers] so then
   * it can use the observer to publish the results so then we can receive those in [onNext] callback.
   */
  private def createStreamObserver(writable: Reader.Writable): StreamObserver[PrimeNumberRequest] = {
    stub.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
      override def onNext(response: PrimeNumberResponse): Unit = {
        logger.debug(s"[PrimerNumberClientImpl] response:$response")
        Await.result(writable.write(Buf.Utf8(response.getValue)))
      }

      override def onError(t: Throwable): Unit = {
        logger.error(s"[PrimerNumberClientImpl] Error in StreamObserver. Caused by ${ExceptionUtils.getStackTrace(t)}")
        writable.fail(t)
      }

      override def onCompleted(): Unit = {
        logger.info("[PrimerNumberClientImpl] StreamObserver completed")
      }
    })
  }
}
