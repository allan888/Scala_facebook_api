package com.xueliu

import akka.actor.{ActorSelection, ActorRef}
import spray.json.DefaultJsonProtocol
import ErrorJsonProtocol._
import TokenJsonProtocol._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.json._
import OKJsonProtocol._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object UserPassJsonProtocol extends DefaultJsonProtocol {

  implicit val userpassFormat = jsonFormat2(UserPass)
}

object UserPassRequestJsonProtocol extends DefaultJsonProtocol {

  implicit val userpassrequestFormat = jsonFormat3(UserPassRequest)
}

case class UserPass(username: String, password:String) {
  def getToken(userActor:ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)
    val future = userActor ? UserPassRequest("getToken",username,password)
    val ini = Await.result(future, Duration.Inf)
    ini match {
      case x:TokenAndId => x
      case x:Error => x
      case _ => {
        Error("unrecognized request")
      }
    }
  }
  def register(userActor:ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)
    val future = userActor ? UserPassRequest("register",username,password)
    val ini = Await.result(future, Duration.Inf)
    ini match {
      case x:ID => x
      case x:Error => x
      case _ => Error("error in registration process")
    }
  }

}

case class UserPassRequest(req:String, username: String, password:String) {
}