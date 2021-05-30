package com.politrons.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

object GatlingHttpClient {

  val host:String = "http://localhost:9994"

  val conf: HttpProtocolBuilder = http

}
