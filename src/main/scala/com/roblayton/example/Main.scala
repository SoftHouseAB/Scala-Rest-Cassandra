package com.roblayton.spray

import akka.actor.ActorSystem
import com.roblayton.example.{ConnectToCassandra, Movies}
import spray.routing.SimpleRoutingApp
import spray.routing.Route
import spray.http.MediaTypes
import spray.httpx.Json4sSupport
import spray.http.{AllOrigins, HttpMethod, HttpMethods, HttpResponse}
import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.routing._
import org.json4s.Formats
import org.json4s.JsonAST.JObject
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{read, write, writePretty}
import com.roblayton.example.config.Configuration

object Main extends App with SimpleRoutingApp with Configuration with Json4sSupport {

  implicit var actorSystem = ActorSystem()

  // globally override the default format to respond with Json
  implicit def json4sFormats: Formats = DefaultFormats

  var fragments = Fragment.fragments
  //var tempData = ConnectToCassandra.demo()


  startServer(interface = serviceHost, port = servicePort) {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        get {
          path("movies") {
            respondWithMediaType(MediaTypes.`application/json`) {
              var temp = new ConnectToCassandra()
              var tempData = temp.demo()
              complete {
                temp.toJSON(tempData)
              }
            }
          }
        } ~
        get {
        path("hello") {
          complete {
            "Hello World!"
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
              val temp = new ConnectToCassandra()
              temp.addMovie(id, name, status)
              complete {
                "OK"
              }
            }
          }
        } ~
        post {
          path("metrics" / "add") {
            parameters("data") { (data) =>
              val temp = new ConnectToCassandra()
              temp.addMetric(data)
              complete {
                "OK"
              }
            }
          }
        }

      }
  }

  //ConnectToCassandra.demo();
}
