package com.politrons.it

import com.politrons.api.ProxyServer
import com.politrons.grpc.PrimeNumberServiceGrpc.PrimeNumberServiceImplBase
import com.politrons.grpc.{PrimeNumberClient, PrimeNumberServiceImpl, PrimerNumberClientImpl, PrimerNumberServer}
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.{Http, http}
import com.twitter.io.{Buf, Reader}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Runtime, ZLayer}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

class PrimeNumberPlatformSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val proxyServerPort = 9994

  override def beforeAll(): Unit = {
    val proxyServerProgram = ProxyServer.start(proxyServerPort)
    val primeNumberClient: PrimeNumberClient = PrimerNumberClientImpl()

    val serverProgram = PrimerNumberServer.createPrimeNumberServer()
    val service: PrimeNumberServiceImplBase = new PrimeNumberServiceImpl()
    Future {
      Runtime.global.unsafeRun(serverProgram.provideLayer(ZLayer.succeed(service)))
    }
    Future {
      Runtime.global.unsafeRun(proxyServerProgram.provideLayer(ZLayer.succeed(primeNumberClient)))
    }
    //TODO:Yeah pretty bad I know, but those server are running async so it's hard to know when they're ready
    Thread.sleep(5000)
  }

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
      val primeNumberLimit = "17"

      client(http.Request(s"/prime/:number", Tuple2("number", primeNumberLimit))).flatMap {
        response =>
          if (response.statusCode != 200) promise.failure(new Exception(s"Server error response. Code ${response.statusCode}"))
          fromReader(response.reader) foreach {
            case Buf.Utf8(primeNumber) =>
              println(s"[PrimeNumberPlatformSpec] Prime number:$primeNumber")
              if (primeNumber == primeNumberLimit) promise.success(primeNumber)
          }
      }
      Then("I receive the prime numbers in the stream")
      val prime = scala.concurrent.Await.result(promise.future, 300 seconds)
      assert(prime != null)
      assert(prime == primeNumberLimit)
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

