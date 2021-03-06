package com.politrons.performance.simulation


import com.politrons.performance.GatlingHttpClient
import com.politrons.performance.scene.PrimeNumberPlatformScene
import io.gatling.core.Predef.{Simulation, _}
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._
import scala.concurrent.duration.{Duration, SECONDS}

/**
 * TODO:WE have to upgrade Gatling to one of the latest version to allow http2 and avoid close the connection
 * For now I will drastically reduce the traffic to avoid too much noise in the logs.
 */
class PrimeNumberPlatformSimulation extends Simulation {

  val idleTime = 1
  val users = 10
  val time = 10

  private val primeNumberScn: ScenarioBuilder = new PrimeNumberPlatformScene().create()

  setUp(
    primeNumberScn.inject(nothingFor(idleTime.seconds), rampUsers(users) over Duration(time, SECONDS)))
    .protocols(GatlingHttpClient.conf)
    .assertions(global.responseTime.max.lessThan(5000))
    .assertions(global.responseTime.mean.lessThan(100))
    .assertions(global.responseTime.percentile3.lessThan(300))
    .assertions(global.responseTime.percentile4.lessThan(2000)
  )

}