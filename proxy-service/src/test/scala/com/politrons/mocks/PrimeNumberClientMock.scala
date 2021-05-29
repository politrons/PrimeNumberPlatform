package com.politrons.mocks

import com.politrons.grpc.PrimeNumberClient
import com.twitter.io.{Buf, Reader}
import zio.{Has, ZIO, ZManaged}

/**
 * Mock class to emulate the real PrimeNumberClientImpl.
 * I could use scalaMock, but due of the lack of time, this a faster solution but of course
 * most verbose.
 */
case class PrimeNumberClientMock() extends PrimeNumberClient {

  private val primes: List[String] = List("2", "3", "5", "7", "11", "13", "17")

  override def findPrimeNumbers(number: String): ZIO[Has[Reader.Writable], Throwable, Unit] = {
    (for {
      writable <- ZManaged.service[Reader.Writable].useNow
      _ <- ZIO.effect {
          primes.foreach(prime => {
            Thread.sleep(500)
            println(s"[PrimerNumberClientMock] Prime:$prime ")
            writable.write(Buf.Utf8(prime))
          })
      }
    } yield ()).catchAll(t => {
      println(s"[PrimerNumberClientMock] Error: Caused by ${t.getCause} ")
      ZIO.fail(t)
    })
  }
}