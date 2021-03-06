package com.politrons.it

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.Future
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Promise}
import scala.util.Try

class PrimeNumberPlatformSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val proxyServerPort = 9994

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  feature("Integration test to prove that end to end between ProxyServer and PrimeNumberServer works ") {

    scenario("End to end request to ProxyServer endpoint with correct number and this one " +
      "call prime number server by gRPC") {
      Given("a Finagle client to call ProxyServer")

      val promise: Promise[String] = Promise()

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$proxyServerPort")

      When("I invoke the endpoint /prime")
      val primeNumberLimit = "31183"

      runRequest(client, primeNumberLimit, promise)
      Then("I receive the prime numbers in the stream")
      val prime = scala.concurrent.Await.result(promise.future, 60 seconds)
      assert(prime != null)
      assert(prime == primeNumberLimit)
    }

    scenario("End to end request to ProxyServer endpoint with wrong number and this one " +
      "call prime number server by gRPC") {
      Given("a Finagle client to call ProxyServer")

      val promise: Promise[String] = Promise()

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$proxyServerPort")

      When("I invoke the endpoint /prime")
      val primeNumberLimit = "foo"

      runRequest(client, primeNumberLimit, promise)
      Then("I receive the prime numbers in the stream")
      val responseTry = Try(scala.concurrent.Await.result(promise.future, 30 seconds))
      assert(responseTry.isFailure)
    }

  }

  private def runRequest(client: Service[Request, Response],
                         primeNumberLimit: String,
                         promise: Promise[String]): Future[Unit] = {
    client(http.Request(s"/prime/:number", Tuple2("number", primeNumberLimit))).flatMap {
      response =>
        if (response.statusCode != 200) promise.failure(new Exception(s"Server error response. Code ${response.statusCode}"))
        fromReader(response.reader) foreach {
          case Buf.Utf8(primeNumber) =>
            println(s"[PrimeNumberPlatformSpec] Prime number:$primeNumber")
            if (primeNumber == primeNumberLimit) promise.success(primeNumber)
        }
    }
  }

  /**
   * Recursive method with escape condition in case the reader has None elements.
   * Otherwise we return a AsyncStream[Buf] and we subscribe again to the next
   * element of the stream to read with [reader.read(Int.MaxValue)]
   */
  def fromReader(reader: Reader): AsyncStream[Buf] =
    AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
      case None =>
        AsyncStream.empty
      case Some(buf) =>
        buf +:: fromReader(reader)
    }
}

