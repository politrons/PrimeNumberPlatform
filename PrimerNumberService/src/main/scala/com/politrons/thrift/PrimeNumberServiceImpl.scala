package com.politrons.thrift

import com.twitter.util.Future

class PrimeNumberServiceImpl extends PrimeNumberService.MethodPerEndpoint {

  def findPrimeNumbers(primeNumber: Long): Future[String] = {
    Future.value(s"$primeNumber")
  }

}