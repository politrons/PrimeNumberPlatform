package com.politrons.performance.scene

import com.politrons.performance.GatlingHttpClient
import io.gatling.core.Predef.{scenario, _}
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{http, status, _}

import scala.concurrent.duration.{Duration, SECONDS}

class PrimeNumberPlatformScene {

  def create(): ScenarioBuilder = {
    scenario("PrimeNumberPlatform")
      .exec(http("PrimeNumberPlatform")
        .get(s"${GatlingHttpClient.host}/prime/:number")
        .queryParamMap(Map("number" -> "2"))
        .check(status.is(200)))
      .pause(Duration(1, SECONDS))
  }

}

