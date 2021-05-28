package com.politrons.grpc

import io.grpc.stub.StreamObserver
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

class PrimeNumberServiceImplSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val promise: Promise[String] = Promise()
  feature("PrimeNumberServiceImpl to generate prime numbers ") {
    scenario("We create the instance of PrimeNumberServiceImpl and we invoke the findPrimeNumbers") {
      Given("An instance of the service")
      val service = new PrimeNumberServiceImpl()
      When("I invoke to get findPrimeNumbers ")
      var primeNumberCount = 0
      val observer: StreamObserver[PrimeNumberRequest] = service.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
        override def onNext(response: PrimeNumberResponse): Unit = {
          System.out.println(s"Prime number response:${response.getValue}")
          primeNumberCount += 1
          if(primeNumberCount >=7){
            promise.success(response.getValue)
          }
        }

        override def onError(t: Throwable): Unit = {
          promise.failure(t)
        }

        override def onCompleted(): Unit = {
          System.out.println("Stream finish")
        }
      })
      val primeNumber = "17"
      val request: PrimeNumberRequest = PrimeNumberRequest.newBuilder.setAttr(primeNumber).build;
      observer.onNext(request)
      Then("it return the expected number of prime numbers")
      assert(Await.result(promise.future, 30 seconds) == primeNumber)
    }
  }

}
