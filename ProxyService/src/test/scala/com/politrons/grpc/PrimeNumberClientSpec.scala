package com.politrons.grpc

import com.politrons.mocks.PrimerNumberServerMock
import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import zio.{Has, Runtime, ZIO, ZLayer}

import java.nio.charset.Charset

class PrimeNumberClientSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    PrimerNumberServerMock.start()
  }

  override def afterAll(): Unit = {
    PrimerNumberServerMock.stop()
  }

  feature("PrimeNumberClientSpec to make a rpc connection against a mock to prove it works ") {
    scenario("Invoke the connector to Make a call to the function [findPrimeNumbers]" +
      "it return a ZIO program ZIO[Has[Reader.Writable], Throwable, Unit] then we evaluate " +
      "the program and we check some data has been written") {
      Given("A Prime number program and Reader.Writable")
      val primeNumber = "17"
      val findPrimeNumbersProgram: ZIO[Has[Reader.Writable], Throwable, Unit] =
        PrimerNumberClient().findPrimeNumbers(primeNumber)
      val writable: Reader.Writable = Reader.writable()
      When("I run the program passing the [Writable] dependency")
      Runtime.global.unsafeRun(findPrimeNumbersProgram.provideLayer(ZLayer.succeed(writable)))
      Then("The Writable contain some data")
      val future: Future[Option[Buf]] = writable.read(Int.MaxValue)
      val maybeBuf = Await.result(future)
      assert(maybeBuf.isDefined)
      val primeNumberResponse = Buf.decodeString(maybeBuf.get, Charset.defaultCharset())
      assert(primeNumberResponse == primeNumber)
    }
  }
}
