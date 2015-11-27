package com.xueliu

import akka.actor.ActorSelection
import akka.util.Timeout
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TimelineJsonProtocol extends DefaultJsonProtocol {

  implicit val timelineFormat = jsonFormat3(Timeline)
}
case class Timeline(ids:Array[Long], previous:Int, next:Int) {
  def toNodeFormat(contentActorSelection: ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)
    val future = contentActorSelection ? IDArray(ids)
    val ret = Await.result(future, Duration.Inf)
    ret match {
      case x:TimelineNode => TimelineNode(x.data,previous,next)
      case x:Error => x
      case _ => Error("get timeline in node format failed")
    }
  }
}