package com.xueliu

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TokenJsonProtocol extends DefaultJsonProtocol {

  implicit val tokenFormat = jsonFormat1(Token)
}

case class Token(access_token: String) {
  def getUserIdAndName(userActor:ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)
    val future = userActor ? this
    val ret = Await.result(future, Duration.Inf)
    ret match {
      case x:IdAndName => x
      case x:Error => x
      case _ => Error("unrecognized response in class Token")
    }
  }
  def getUserIdAndNameAndPublic(userActor:ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)
    val future = userActor ? ("getPublic",this)
    val ret = Await.result(future, Duration.Inf)
    ret match {
      case x:IdAndNameAndPublic => x
      case x:Error => x
      case _ => Error("unrecognized response in class Token")
    }
  }
}