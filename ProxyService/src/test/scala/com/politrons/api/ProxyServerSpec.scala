package com.politrons.api

import com.politrons.grpc.PrimeNumberClient
import com.politrons.mocks.{PrimeNumberClientMock, PrimerNumberServerMock}
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.{Http, http}
import com.twitter.io.{Buf, Reader}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZLayer}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Try

class ProxyServerSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    PrimerNumberServerMock.start()
  }

  override def afterAll(): Unit = {
    PrimerNumberServerMock.stop()
  }

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  feature("ProxyServer to return a stream with the prime numbers ") {

    scenario("ProxyServer prime endpoint with correct number") {
      Given("a proxy server and a mock prime number server")

      val port = 1981
      val promise: Promise[String] = Promise()
      val proxyServerProgram = ProxyServer.start(port)
      val primeNumberClient: PrimeNumberClient = PrimeNumberClientMock()
      Future {
        Runtime.global.unsafeRun(proxyServerProgram.provideLayer(ZLayer.succeed(primeNumberClient)))
      }

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$port")

      When("I invoke the endpoint /prime")
      val primeNumberLimit = "17"

      client(http.Request(s"/prime/:number", Tuple2("number", primeNumberLimit))).flatMap {
        response =>
          if(response.statusCode != 200) promise.failure(new Exception(s"Server error response. Code ${response.statusCode}"))
          fromReader(response.reader) foreach {
            case Buf.Utf8(buf) =>
              promise.success(buf)
          }
      }
      Then("I receive the prime numbers in the stream")
      val prime = scala.concurrent.Await.result(promise.future, 30 seconds)
      assert(prime != null)
      assert(prime == primeNumberLimit)
    }


    scenario("ProxyServer prime endpoint with wrong number") {
      Given("a proxy server and a mock prime number server")

      val port = 1981
      val promise: Promise[String] = Promise()
      val proxyServerProgram = ProxyServer.start(port)
      val primeNumberClient: PrimeNumberClient = PrimeNumberClientMock()
      Future {
        Runtime.global.unsafeRun(proxyServerProgram.provideLayer(ZLayer.succeed(primeNumberClient)))
      }

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$port")

      When("I invoke the endpoint /prime")
      val primeNumberLimit = "foo"

      client(http.Request(s"/prime/:number", Tuple2("number", primeNumberLimit))).flatMap {
        response =>
          if(response.statusCode != 200) promise.failure(new Exception(s"Server error response. Code ${response.statusCode}"))
          fromReader(response.reader) foreach {
            case Buf.Utf8(buf) =>
              promise.success(buf)
          }
      }
      Then("I receive the prime numbers in the stream")
      val primeTry = Try(scala.concurrent.Await.result(promise.future, 30 seconds))
      assert(primeTry.isFailure)
      assert(primeTry.failed.get.getMessage.contains("Server error response"))
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
