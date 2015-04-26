package com.roblayton.example.config

import com.typesafe.config.ConfigFactory
import util.Try

trait Configuration {
  val config = ConfigFactory.load()

  lazy val serviceHost = Try(config.getString("service.host")).getOrElse("localhost")
  lazy val servicePort = Try(config.getInt("service.port")).getOrElse(8080)
}
