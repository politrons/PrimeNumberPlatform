package com.politrons.api

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.{Http, http}
import com.twitter.io.{Buf, Reader}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import scala.concurrent.Promise
import scala.concurrent.duration._

class ProxyServerSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val port = 1981

  val promise: Promise[String] = Promise()

  feature("ProxyServer to return a stream with the prime numbers ") {
    scenario("ProxyServer prime endpoint") {
      Given("a proxy server and a mock prime number server")

      ProxyServer.start(port)

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$port")
      //TODO:Once we have the logic to communicate to the other service add a mock here

      When("I invoke the endpoint /prime")
      client(http.Request(http.Method.Get, "/prime")).flatMap {
        response =>
          fromReader(response.reader) foreach {
            case Buf.Utf8(buf) =>
              promise.success(buf)
          }
      }
      Then("I receive the prime numbers in the stream")
      val prime = scala.concurrent.Await.result(promise.future, 30 seconds)
      assert(prime != null)
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
