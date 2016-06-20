package com.roblayton.spray

import akka.actor.ActorSystem
import com.roblayton.example.{ConnectToCassandra, Metrics}
import spray.routing.SimpleRoutingApp
import spray.routing.Route
import spray.http._
import spray.httpx.Json4sSupport
import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.routing._
import org.json4s.Formats
import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, write, writePretty}
import com.roblayton.example.config.Configuration

object Main extends App with SimpleRoutingApp with Configuration with Json4sSupport {

  implicit var actorSystem = ActorSystem()

  // globally override the default format to respond with Json
  implicit def json4sFormats: Formats = DefaultFormats

  startServer(interface = serviceHost, port = servicePort) {
    respondWithHeaders(RawHeader("Access-Control-Allow-Origin", "*"),
                       RawHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE"),
                       RawHeader("Access-Control-Allow-Headers","X-Requested-With,Origin,Content-Type,X-Auth-Token")) {
      get {
        path("hello") {
          complete {
            "Hello World!"
          }
        }
      } ~
      get {
        path("metrics") {
          parameters("ip", "sdate", "edate") { (ip, sdate, edate) =>
            complete {
              ConnectToCassandra.toJSONMM(ConnectToCassandra.getMetrics(ip, sdate, edate))
            }
          }
        }
      } ~
      get {
        path("metrics" / "devices") {
          complete {
            ConnectToCassandra.toJSOND(ConnectToCassandra.getDevices())
          }
        }
      } ~
      get {
        path("metrics") {
          complete {
            ConnectToCassandra.toJSONM(ConnectToCassandra.getMetrics())
          }
        }
      } ~
      delete {
        path("metrics" / "delete") {
          parameters("ip", "date") { (ip, date) =>
            ConnectToCassandra.delMetric(ip, date)
            complete {
              "Device Deleted!"
            }
          }
        }
      } ~
      delete {
        path("metrics" / "delete") {
          parameter("ip") { (ip) =>
            ConnectToCassandra.delDevice(ip)
              complete {
                "Device Deleted!"
              }
          }
        }
      } ~
      post {
        path("metrics" / "add") {
          entity(as[JObject]) { metricObj =>
            val metric = metricObj.extract[Metrics]
            ConnectToCassandra.addMetric(metric)
            complete {
              "OK"
            }
          }
        }
      }
    }
  }
}
