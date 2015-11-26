package com.xueliu
import akka.actor._
import scala.collection.mutable.{ListBuffer, HashMap}

import scala.collection.mutable

/**
  * Created by xueliu on 11/21/15.
  */
class FriendsListActor() extends Actor{

  val friendsListDB = new HashMap[Long,ListBuffer[Long]]()
  friendsListDB += (1000001L -> new ListBuffer[Long])
  1000002L +=: friendsListDB(1000001L)

  friendsListDB += (1000002L -> new ListBuffer[Long])
  1000001L +=: friendsListDB(1000002L)

  def getList(uid:ID) = {
    friendsListDB.get(uid.id) match {
      case Some(f_list) => IDArray(f_list.toArray)
      case None => Error("user not exists")
    }
  }

  def addFriends(uid:Long,f_list:Array[Long]) = {
    friendsListDB.get(uid) match {
      case Some(user) => {
        for(i <- 0 until f_list.length){
          f_list(i) +=: friendsListDB(1000002L)
        }
        OK("add friends finished")
      }
      case None => Error("user not exists")
    }
  }

  def receive = {
    case user:ID => {
      sender ! getList(user)
    }
    case RequestIdId(req,id,ids) => {
      req match {
        case "add" => {
          sender ! addFriends(id,ids.ids)
        }
        case _ => sender ! Error("unsupported friendsListActor request.")
      }
    }
    case _ => sender ! Error("unsupported friendsListActor request.")
  }
}
