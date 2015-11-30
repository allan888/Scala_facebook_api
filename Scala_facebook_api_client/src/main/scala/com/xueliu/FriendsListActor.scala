package com.xueliu
import akka.actor._
import scala.collection.mutable.{ListBuffer, HashMap}

import scala.collection.mutable

/**
  * Created by xueliu on 11/21/15.
  */
class FriendsListActor() extends Actor{

  val friendsListDB = new HashMap[Long,ListBuffer[IdAndName]]()
  friendsListDB += (1000001L -> new ListBuffer[IdAndName])
  IdAndName(1000002L,"yazhang") +=: friendsListDB(1000001L)

  friendsListDB += (1000002L -> new ListBuffer[IdAndName])
  IdAndName(1000001L,"xueliu") +=: friendsListDB(1000002L)

  def getList(uid:Long) = {
    friendsListDB.get(uid) match {
      case Some(f_list) => IdAndNameArray(f_list.toArray)
      case None => Error("user not exists")
    }
  }

  def register(uid:Long) = {
    friendsListDB.get(uid) match {
      case Some(f_list) => Error("user already exists")
      case None => {
        friendsListDB += (uid -> new ListBuffer[IdAndName])
        OK("registration succeed")
      }
    }
  }

  def addFriend(uid:Long,id_name:IdAndName) = {
    if( friendsListDB.keySet.exists(_ == uid) && friendsListDB.keySet.exists(_ == id_name.id) && uid != id_name.id){
      if(!friendsListDB.exists({ case (x:Long,y:ListBuffer[IdAndName]) => (x == uid && y.contains(id_name)) } ) ){
        id_name +=: friendsListDB(uid)
        OK("add friends finished")
      }else{
        Error("they are friends already")
      }
    }else{
      Error("user not exists")
    }
  }
  def delFriend(me:Long,other:Long) = {
    if( friendsListDB.keySet.exists(_ == me) && friendsListDB.keySet.exists(_ == other) && me != other){
      friendsListDB.get(me) match {
        case Some(me_list) => {
          friendsListDB.get(other) match {
            case Some(other_list) => {
              friendsListDB += (me -> me_list.filter(_.id != other))
              friendsListDB += (other -> other_list.filter(_.id != me))
              OK("delete friend done")
            }
            case None => Error("your are not friend")
          }
        }
        case None => Error("your are not friend")
      }
    }else{
      Error("user not exists")
    }
  }

  def receive = {
    case RequestId(req,id) => {
      req match {
        case "get" => sender ! getList(id)
        case "register" => sender ! register(id)
      }
    }
    case RequestIdIdAndName(req,id,id_name) => { // id = me, id_name = other
      req match {
        case "add" => {
          sender ! addFriend(id,id_name)
        }
        case "del" => {
          sender ! delFriend(id_name.id,id)
        }
        case _ => sender ! Error("unsupported friendsListActor request.")
      }
    }
    case _ => sender ! Error("unsupported friendsListActor request.")
  }
}
