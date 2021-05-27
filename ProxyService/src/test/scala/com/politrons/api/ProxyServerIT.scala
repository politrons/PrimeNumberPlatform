package com.politrons.api

import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Http, http}
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Await, Future}

class ProxyServerIT extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  val port = 1981

  feature("ProxyServer to return a stream with the prime numbers ") {
    scenario("ProxyServer prime endpoint") {
      Given("a proxy server and a mock prime number server")

      ProxyServer.start(port)

      val client = Http.client
        .withStreaming(enabled = true)
        .newService(s"/$$/inet/localhost/$port")
      //TODO:Once we have the logic to communicate to the other service add a mock here

      When("I invoke the endpoint /prime")
      val future: Future[Unit] = client(http.Request(http.Method.Get, "/prime")).flatMap {
        response =>
          fromReader(response.reader) foreach {
            case Buf.Utf8(buf) =>
              println(buf)
          }
      }


      Await.result(future)
      Thread.sleep(10000000)

      Then("I receive the prime numbers in the stream")
    }
  }


  def fromReader(reader: Reader): AsyncStream[Buf] =
    AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
      case None =>
        AsyncStream.empty
      case Some(a) =>
        a +:: fromReader(reader)
    }
}
