package com.roblayton.spray

import akka.actor.ActorSystem

import spray.routing.SimpleRoutingApp
import spray.routing.Route
import spray.http.MediaTypes
import spray.httpx.Json4sSupport

import org.json4s.Formats
import org.json4s.JsonAST.JObject
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.{ read, write, writePretty }

import com.roblayton.example.config.Configuration

object Main extends App with SimpleRoutingApp with Configuration with Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
  implicit var actorSystem = ActorSystem()

  var fragments = Fragment.fragments

  startServer(interface = serviceHost, port = servicePort) {
    get {
      path("hello") {
        complete {
          "Hello World!"
        }
      }
    } ~
    get {
      path("fragments") {
        respondWithMediaType(MediaTypes.`application/json`) {
          complete {
            fragments
          }
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
    }
  }
}
