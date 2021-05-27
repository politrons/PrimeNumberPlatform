package com.politrons.thrift

import com.twitter.util.{Await, Future}
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}

class PrimeNumberServiceImplSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  feature("PrimeNumberServiceImpl to generate prime numbers ") {
    scenario("sss") {
      Given("An instance of the service")
      val service = new PrimeNumberServiceImpl()
      When("I invoke to get the prime numbers ")
      val primeNumbers: Future[String] = service.findPrimeNumbers(19)
      Then("it return the expected number of prime numbers")
      assert(Await.result(primeNumbers) == "19")
    }
  }

}
