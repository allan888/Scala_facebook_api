package com.xueliu

import java.util.Calendar

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MessageWithTokenAndIdJsonProtocol extends DefaultJsonProtocol {

  implicit val messagewithtokenandidFormat = jsonFormat3(MessageWithTokenAndId)
}
case class MessageWithTokenAndId(message: String, access_token:String,uid:Long) {
  def postMessage(userActorSelection:ActorSelection, contentActorSelection:ActorSelection, friendsListActorSelection:ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)

    val future1 = userActorSelection ? Token(access_token)
    val ret1 = Await.result(future1, Duration.Inf)
    ret1 match {
      case IdAndName(got_id, got_name) => {
        got_id == uid match {
          case true => {
            val future2 = contentActorSelection ? PostNoId(uid, got_name, "post", Calendar.getInstance.getTime.toString, message,"")
            val ret2 = Await.result(future2, Duration.Inf)
            ret2 match {
              case ID(p_id) => { // p_id is post id
                val future3 = friendsListActorSelection ? ID(uid)
                val ret3 = Await.result(future3, Duration.Inf)
                ret3 match {
                  case IDArray(f_list) => { //
                    val future4 = userActorSelection ? RequestIdId("addPost", p_id, IDArray(uid +: f_list))
                    val ret4 = Await.result(future4, Duration.Inf)
                    ret4 match {
                      case x:OK => ID(p_id)
                      case x:Error => x
                      case _ => Error("add post failed")
                    }
                  }
                }

              }
              case x:Error => x
              case _ => Error("add post failed")
            }
          }
          case false => Error("token does not match user id")
        }

      }
      case x:Error => x
      case _ => Error("unrecognized response in class Token")
    }

  }
}