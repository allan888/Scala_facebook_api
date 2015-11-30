package com.xueliu

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.routing.RoundRobinRouter
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Main {

  def main(args: Array[String]) {

    if (args.length == 0) {
      // run test case
      implicit val system = ActorSystem("facebook")


      //----------user server------------
      val userService = system.actorOf(Props[UserActor], "userService")
      //----------user server------------

      //----------content server------------
      val contentService = system.actorOf(Props[ContentActor], "contentService")
      //----------content server------------

      //----------friends list server------------
      val friendsListService = system.actorOf(Props[FriendsListActor], "friendsListService")
      //----------friends list server------------


      //----------web server------------
      // create and start our service actor
      var routees: List[ActorRef] = List()
      for (i <- 1 to 300) {
        routees = system.actorOf(Props[FacebookGoActor]) +: routees
      }
      val fbService = system.actorOf(Props[FacebookGoActor].withRouter(RoundRobinRouter(routees = routees)))

      implicit val timeout = Timeout(5.seconds)
      // start a new HTTP server on port 8080 with our service actor as the handler
      IO(Http) ? Http.Bind(fbService, interface = "localhost", port = 8080)
      //----------web server------------


    } else {

      if (args(0) == "webServer") {


        // we need an ActorSystem to host our application in
        implicit val system = ActorSystem("facebook")

        // create and start our service actor
        var routees: List[ActorRef] = List()
        for (i <- 1 to 300) {
          routees = system.actorOf(Props[FacebookGoActor]) +: routees
        }
        val fbService = system.actorOf(Props[FacebookGoActor].withRouter(RoundRobinRouter(routees = routees)))

        implicit val timeout = Timeout(5.seconds)
        // start a new HTTP server on port 8080 with our service actor as the handler
        IO(Http) ? Http.Bind(fbService, interface = "localhost", port = 8080)


      } else if (args(0) == "userServer") {

        implicit val system = ActorSystem("facebook")
        val userService = system.actorOf(Props[UserActor], "userService")

      } else if (args(0) == "contentServer") {

        implicit val system = ActorSystem("facebook")
        val userService = system.actorOf(Props[ContentActor], "contentService")

      } else if (args(0) == "friendsListServer") {

        implicit val system = ActorSystem("facebook")
        val friendsListService = system.actorOf(Props[FriendsListActor], "friendsListService")

      }
    }
  }

}
