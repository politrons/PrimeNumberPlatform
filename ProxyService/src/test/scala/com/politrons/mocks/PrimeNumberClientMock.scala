package com.politrons.mocks

import com.politrons.grpc.PrimeNumberClient
import com.twitter.io.{Buf, Reader}
import zio.{Has, ZIO, ZManaged}

/**
 * Mock class to emulate the real PrimeNumberClientImpl.
 * I could use scalaMock, but due of the lack of time, this a faster solution but of course
 * most verbose.
 */
case class PrimeNumberClientMock() extends PrimeNumberClient{
  override def findPrimeNumbers(number: String): ZIO[Has[Reader.Writable], Throwable, Unit] = {
    (for {
      writable <- ZManaged.service[Reader.Writable].useNow
      _ <- ZIO.effect {
        val buf = Buf.Utf8(number)
        writable.write(buf)
      }
    } yield ()).catchAll(t => {
      println(s"[PrimerNumberClientMock] Error: Caused by ${t.getCause} ")
      ZIO.fail(t)
    })
  }
}
