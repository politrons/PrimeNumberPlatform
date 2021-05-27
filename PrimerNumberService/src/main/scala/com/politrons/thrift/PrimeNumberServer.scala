package com.politrons.thrift

import com.twitter.finagle.Thrift
import com.twitter.util.Await

/**
 * In order to use Thrift service we need to use a Thrift server.
 * Here we add the service just like we do in regular finagle http server.
 */
object PrimeNumberServer extends App {

  def start(port: Int): Unit = {
    val server = Thrift.server.serveIface(s"localhost:$port", new PrimeNumberServiceImpl)
    Await.ready(server)
  }

  start(8000)
}
