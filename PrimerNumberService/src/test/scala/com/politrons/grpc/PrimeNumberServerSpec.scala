package com.politrons.grpc

import io.grpc.stub.StreamObserver
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.language.postfixOps

//TODO:Think about doing it better in a IT test
//class PrimeNumberServerSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {
//
//  val promise: Promise[String] = Promise()
//  feature("PrimeNumberServer to check that server run as we expect ") {
//    scenario("I start the PrimeNumberServer and I connect with a client") {
//      Given("A server running")
//      val service = new PrimeNumberServiceImpl()
//      When("I invoke to get findPrimeNumbers ")
//      val observer: StreamObserver[PrimeNumberRequest] = service.findPrimeNumbers(new StreamObserver[PrimeNumberResponse]() {
//        override def onNext(value: PrimeNumberResponse): Unit = {
//          promise.success(value.getValue)
//        }
//
//        override def onError(t: Throwable): Unit = {
//          promise.failure(t)
//        }
//
//        override def onCompleted(): Unit = {
//          System.out.println("Stream finish")
//        }
//      })
//      val primeNumber = "17"
//      val request: PrimeNumberRequest = PrimeNumberRequest.newBuilder.setAttr(primeNumber).build;
//      observer.onNext(request)
//      Then("it return the expected number of prime numbers")
//      assert(Await.result(promise.future, 30 seconds) == primeNumber)
//    }
//  }
//
//}
