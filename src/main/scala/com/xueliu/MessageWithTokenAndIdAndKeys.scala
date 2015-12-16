package com.xueliu

import java.util.Calendar

import akka.actor.{ActorSelection, ActorRef}
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MessageWithTokenAndIdAndKeysJsonProtocol extends DefaultJsonProtocol {

  implicit val messagewithtokenandidandkeysFormat = jsonFormat7(MessageWithTokenAndIdAndKeys)
}

case class MessageWithTokenAndIdAndKeys(message: String, typ: String, photoData: String, albumId: Long, access_token: String, uid: Long, keys:Map[String,String]) {
  def postMessage(userActorSelection: ActorSelection, contentActorSelection: ActorSelection, friendsListActorSelection: ActorSelection) = {
    implicit val timeout = Timeout(10 seconds)

    val future1 = userActorSelection ? Token(access_token)
    val ret1 = Await.result(future1, Duration.Inf)
    ret1 match {
      case IdAndName(got_id, got_name) => {
        //println("gotten id:"+got_id.toString)
        if (got_id == uid) {
          var album_id = 0L

          if (typ == "photo") {
            if(albumId == 0L){
              val future4 = userActorSelection ?("getDefaultAlbumId", uid)
              val ret4 = Await.result(future4, Duration.Inf)
              ret4 match {
                case ID(ret_album_id) => {
                  //println(uid.toString+"'s default album is:"+ret_album_id.toString)
                  album_id = ret_album_id
                }
              }
            }else{
              album_id = albumId
            }
          }

          val future2 = contentActorSelection ? PostNoId(uid, got_name, typ, Calendar.getInstance.getTime.toString, message, photoData, album_id, keys)
          val ret2 = Await.result(future2, Duration.Inf)
          ret2 match {
            case ID(p_id) => {
              // p_id is post id
              val future3 = friendsListActorSelection ? RequestId("get", uid)
              val ret3 = Await.result(future3, Duration.Inf)
              ret3 match {
                case IdAndNameAndPublicArray(f_list) => {
                  if (typ == "post") {
                    val id_list = f_list.map(f => f.id)
                    val future4 = userActorSelection ? RequestIdId("addPost", p_id, IDArray(uid +: id_list))
                    val ret4 = Await.result(future4, Duration.Inf)
                    ret4 match {
                      case x: OK => ID(p_id)
                      case x: Error => x
                      case _ => Error("add post failed")
                    }
                  } else if (typ == "photo") {
                    val id_list = f_list.map(f => f.id)
                    if (albumId > 0L) {
                      val future4 = userActorSelection ? RequestPhotoInfoId("addPhoto", p_id, album_id, IDArray(uid +: id_list))
                      val ret4 = Await.result(future4, Duration.Inf)
                      ret4 match {
                        case x: OK => ID(p_id)
                        case x: Error => x
                        case _ => Error("add post failed")
                      }
                    } else {
                      // default
                      val future5 = userActorSelection ? RequestPhotoInfoId("addPhoto", p_id, album_id, IDArray(uid +: id_list))
                      val ret5 = Await.result(future5, Duration.Inf)
                      ret5 match {
                        case x: OK => ID(p_id)
                        case x: Error => x
                        case _ => Error("add post failed")
                      }
                    }
                  }

                }
              }
            }
            case x => Error("error occurs when posting message(MessageWithTokenAndId)")
          }
        } else {
          Error("token does not match user id")
        }
      }
      case x: Error => x
      case _ => Error("unrecognized response in class Token")
    }

  }

}