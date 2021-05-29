package com.politrons.grpc

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Has, Runtime, ZIO, ZLayer}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}
import scala.language.postfixOps

class PrimeNumberServerSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  val promise: Promise[String] = Promise()
  feature("PrimeNumberServer to check that server run as we expect ") {
    scenario("I start the PrimeNumberServer, passing a mock service, " +
      "and then I connect with a mock client") {
      Given("A server program")
      val serverProgram: ZIO[Has[PrimeNumberServiceGrpc.PrimeNumberServiceImplBase], Throwable, Unit] =
        PrimerNumberServer.createPrimeNumberServer()
      When("I evaluate the server program ")
      val serviceMock: PrimeNumberServiceGrpc.PrimeNumberServiceImplBase = PrimeNumberServiceMock()
      Future {
        Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(serviceMock)))
      }

      And("I make request to the server ")
      //TODO:I know I know! This is really bad!
      Thread.sleep(5000)
      val primeNumber = "17"
      PrimeNumberServerSpec.runRequest(primeNumber)
      Then("it return the expected number of prime numbers")
      assert(Await.result(promise.future, 30 seconds) == primeNumber)
    }
  }

  case class PrimeNumberServiceMock() extends PrimeNumberServiceGrpc.PrimeNumberServiceImplBase {

    override def findPrimeNumbers(responseObserver: StreamObserver[PrimeNumberResponse]): StreamObserver[PrimeNumberRequest] = {
      new StreamObserver[PrimeNumberRequest]() {
        override def onNext(value: PrimeNumberRequest): Unit = {
          System.out.println("[PrimeNumberServiceMock] Prime number from client: " + value.getAttr)
          promise.success(value.getAttr)
        }

        override def onError(t: Throwable): Unit = {
          promise.failure(t)
        }

        override def onCompleted(): Unit = {
        }
      }
    }

  }
}

object PrimeNumberServerSpec {

  def runRequest(primeNumber: String): Unit = {

    val channel = ManagedChannelBuilder
      .forAddress("localhost", 9995)
      .usePlaintext(true).asInstanceOf[ManagedChannelBuilder[_]]
      .build()

    val stub: PrimeNumberServiceGrpc.PrimeNumberServiceStub = PrimeNumberServiceGrpc.newStub(channel)

    val request: PrimeNumberRequest = PrimeNumberRequest.newBuilder.setAttr(primeNumber).build
    val stream: StreamObserver[PrimeNumberRequest] =
      stub.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
        override def onNext(response: PrimeNumberResponse): Unit = {
          System.out.println(s"Prime number mock client Stream on next ${response.getValue}")
        }

        override def onError(t: Throwable): Unit = {
          throw t
        }

        override def onCompleted(): Unit = {
          System.out.println("Prime number mock client Stream finish")
        }
      })

    stream.onNext(request)
  }
}


