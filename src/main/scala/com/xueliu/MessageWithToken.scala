package com.xueliu

import java.util.Calendar

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MessageWithTokenJsonProtocol extends DefaultJsonProtocol {

  implicit val messagewithtokenFormat = jsonFormat2(MessageWithToken)
}
case class MessageWithToken(message: String, access_token:String)