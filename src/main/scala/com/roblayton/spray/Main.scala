package com.roblayton.spray

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp
import spray.routing.Route
import spray.http.MediaTypes

object Main extends App with SimpleRoutingApp {
  implicit var actorSystem = ActorSystem()

  var fragments = Fragment.fragments

  startServer(interface = "localhost", port = 8080) {
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
            Fragment.toJson(fragments)
          }
        }
      }
    } ~
    get {
      path("fragment" / IntNumber / "details") { index =>
        complete {
          Fragment.toJson(fragments(index))
        }
      }
    } ~
    post {
      path("fragment") {
        parameters("name"?, "kind"?, "weight".as[Double]) { (name, kind, weight) =>
          val newFragment = MineralFragment(
            name.getOrElse("mineral"),
            kind.getOrElse("Mineral"),
            weight)

          fragments = newFragment :: fragments

          complete {
            "OK"
          }
        }
      }
    }
  }
}
