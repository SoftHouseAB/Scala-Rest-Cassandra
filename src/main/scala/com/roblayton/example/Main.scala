package com.roblayton.spray

import akka.actor.ActorSystem
import com.roblayton.example.{ConnectToCassandra, Metrics, Movies}
import spray.routing.SimpleRoutingApp
import spray.routing.Route
import spray.http.MediaTypes
import spray.httpx.Json4sSupport
import spray.http.{AllOrigins, HttpMethod, HttpMethods, HttpResponse}
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

  var fragments = Fragment.fragments

  startServer(interface = serviceHost, port = servicePort) {
    respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
      get {
        path("hello") {
          complete {
            "Hello World!"
          }
        }
      } ~
      get {
        path("movies") {
          complete {
            ConnectToCassandra.toJSON(ConnectToCassandra.demo())
          }
        }
      } ~
      get {
        path("metrics") {
          complete {
            ConnectToCassandra.toJSONM(ConnectToCassandra.demoMetrics())
          }
        }
      } ~
      get {
        path("fragments") {
          complete {
            fragments
          }
        }
      } ~
      get {
        path("fragment" / IntNumber) { index =>
          complete {
            fragments(index)
          }
        }
      } ~
      post {
        path("fragment") {
          entity(as[JObject]) { fragmentObj =>
            val fragment = fragmentObj.extract[MineralFragment]
            fragments = fragment :: fragments
            complete {
              "OK"
            }
          }
        }
      } ~
      post {
        path("movies" / "add") {
          parameters("id".as[Int], "name", "status") { (id, name, status) =>
            ConnectToCassandra.addMovie(id, name, status)
            complete {
              "OK"
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
