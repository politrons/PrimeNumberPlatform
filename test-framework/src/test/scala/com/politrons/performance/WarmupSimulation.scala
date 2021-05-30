package com.politrons.performance

import com.politrons.performance.scene.PrimeNumberPlatformScene
import io.gatling.core.Predef.{Simulation, _}
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration._
import scala.concurrent.duration.{Duration, SECONDS}

class WarmupSimulation extends Simulation {

  val idleTime = 1
  val users = 10
  val time = 10

  private val primeNumberScn: ScenarioBuilder = new PrimeNumberPlatformScene().create()

  setUp(
    primeNumberScn.inject(nothingFor(idleTime.seconds), rampUsers(users) over Duration(time, SECONDS))
      .protocols(GatlingHttpClient.conf)
  )

}

