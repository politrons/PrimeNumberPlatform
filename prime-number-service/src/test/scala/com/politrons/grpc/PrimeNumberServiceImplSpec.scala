package com.politrons.grpc

import io.grpc.stub.StreamObserver
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

class PrimeNumberServiceImplSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  feature("PrimeNumberServiceImpl to generate prime numbers ") {
    scenario("We invoke the findPrimeNumbers with a good number") {
      Given("An instance of the service")
      val promise: Promise[Array[String]] = Promise()
      val primes: Array[String] = Array("2", "3", "5", "7", "11", "13", "17")
      val service = new PrimeNumberServiceImpl()
      When("I invoke to get findPrimeNumbers ")
      var index = 0
      val responsePrimes: Array[String] = new Array(7)
      val observer: StreamObserver[PrimeNumberRequest] = service.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
        override def onNext(response: PrimeNumberResponse): Unit = {
          System.out.println(s"Prime number response:${response.getValue}")
          responsePrimes(index) = response.getValue
          index += 1
          if (index >= 7) {
            promise.success(responsePrimes)
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
      assert(Await.result(promise.future, 30 seconds).deep == primes.deep)
    }

    scenario("We invoke the findPrimeNumbers with wrong number") {
      Given("An instance of the service")
      val promise: Promise[Boolean] = Promise()
      val service = new PrimeNumberServiceImpl()
      When("I invoke to get findPrimeNumbers ")
      val observer: StreamObserver[PrimeNumberRequest] = service.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
        override def onNext(response: PrimeNumberResponse): Unit = {
          promise.failure(new IllegalStateException())
        }

        override def onError(t: Throwable): Unit = {
          promise.success(true)
        }

        override def onCompleted(): Unit = {
          System.out.println("Stream finish")
        }
      })
      val primeNumber = "foo"
      val request: PrimeNumberRequest = PrimeNumberRequest.newBuilder.setAttr(primeNumber).build;
      observer.onNext(request)
      Then("it return the expected number of prime numbers")
      assert(Await.result(promise.future, 30 seconds))
    }
  }

}
