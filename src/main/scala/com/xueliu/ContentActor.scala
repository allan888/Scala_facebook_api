package com.xueliu
import akka.actor._
import scala.collection.mutable.{ListBuffer, HashMap}
import java.util.{Calendar}

import scala.collection.mutable

/**
  * Created by xueliu on 11/21/15.
  */
class ContentActor extends Actor{
  var contentID = 5000005L

  // entry -> username, type, title, content
  val contentDB = new HashMap[Long,PostNoId]()
  // add some default users, data has not been persisted.
  contentDB += (5000001L -> PostNoId(1000001L, "xueliu", "post",Calendar.getInstance.getTime.toString,"sample content 1","aux content 1"))
  contentDB += (5000002L -> PostNoId(1000001L, "xueliu", "post",Calendar.getInstance.getTime.toString,"sample content 2","aux content 2"))
  contentDB += (5000003L -> PostNoId(1000002L, "yazhang", "post",Calendar.getInstance.getTime.toString,"sample content 3","aux content 3"))
  contentDB += (5000004L -> PostNoId(1000002L, "yazhang", "post",Calendar.getInstance.getTime.toString,"sample content 4","aux content 4"))

  def getPost(id:Long) = {
    contentDB.get(id) match {
      case Some(post) => post
      case None => Error("post not found")
    }
  }

  def addPost(post:PostNoId) = {
    val newId = contentID
    contentID += 1
    contentDB += (newId ->post)
    ID(newId)
  }

  def receive = {
    case x:PostNoId => sender ! addPost(x)
    case ID(id) => sender ! getPost(id)

    case IDArray(ids) => {
      val newTimeline:Array[Map[String,String]] =
        new Array[Map[String,String]](ids.length)

      for(i <- 0 until ids.length){
        contentDB.get(ids(i)) match {
          case Some(x) => {
            newTimeline(i) = Map(
              "creator_id" -> x.userid.toString,
              "post_id" -> ids(i).toString,
              "name" -> x.username,
              "type" -> x.typ,
              "created_time" -> x.date,
              "content" -> x.content
            )
          }
          case None => {
            newTimeline(i) = Map(
              "id" -> ids(i).toString,
              "content" -> "not found"
            )
          }
        }
      }
      sender ! TimelineNode(newTimeline,"","")
    }
    case other => {
      println(other.getClass)
      sender ! Error("error in content server")
    }
  }
}