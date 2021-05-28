package com.politrons.grpc

import com.twitter.io.Reader
import zio.{Has, ZIO}

trait PrimeNumberClient {

  def findPrimeNumbers(number: String): ZIO[Has[Reader.Writable], Throwable, Unit]

}
