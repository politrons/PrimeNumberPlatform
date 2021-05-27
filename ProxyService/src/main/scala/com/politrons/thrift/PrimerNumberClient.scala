package com.politrons.thrift

import com.twitter.finagle.Thrift
import com.twitter.util.{Await, Future}

/**
 * Just like gRPC with proto, thrift generate the Service and methods specify in the IDL
 * Here we Use the Service [[PrimeNumberService]] with the access type [[com.politrons.thrift.PrimeNumberService.MethodPerEndpoint]]
 *
 * Once that we have the service access we can invoke the remote method
 */
object PrimerNumberClient  extends App {

  def run(port: Int): Unit = {
    val methodPerEndpoint: PrimeNumberService.MethodPerEndpoint =
      Thrift.client.build[PrimeNumberService.MethodPerEndpoint](s"localhost:$port")
    val future: Future[String] = methodPerEndpoint.findPrimeNumbers(19)
    println(Await.result(future))
  }
  run(8000)

}
