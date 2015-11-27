package com.xueliu
import akka.actor._
import scala.collection.mutable.{ListBuffer, HashMap}
import java.security.MessageDigest

import scala.collection.mutable

/**
  * Created by xueliu on 11/21/15.
  */
class UserActor() extends Actor{
  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }

  var id_now:Long = 1000003L

  // nodeID -> (username, md5(password) )
  val userDB_by_id = new HashMap[Long,(String,String)]()
  // add some default users, data has not been persisted.
  userDB_by_id += (1000001L -> ("xueliu",md5("xueliu"+"123456")))
  userDB_by_id += (1000002L -> ("yazhang", md5("yazhang"+"654321")))

  // nodeID -> (username, md5(password) )
  val userDB_by_name = new HashMap[String,Long]()
  // add some default users, data has not been persisted.
  userDB_by_name += ("xueliu"->1000001L)
  userDB_by_name += ("yazhang"->1000002L)

  // username -> id,type
  // post id +=: ListBuffer to prepend a new post to user's timeline
  val userPageDB = new mutable.HashMap[Long,ListBuffer[Long]]()

  userPageDB += (1000001L -> new ListBuffer[Long])
  5000001L +=: userPageDB(1000001L)
  5000002L +=: userPageDB(1000001L)
  5000003L +=: userPageDB(1000001L)
  5000004L +=: userPageDB(1000001L)

  userPageDB += (1000002L -> new ListBuffer[Long])
  5000001L +=: userPageDB(1000002L)
  5000002L +=: userPageDB(1000002L)
  5000003L +=: userPageDB(1000002L)
  5000004L +=: userPageDB(1000002L)

  // token -> username
  val tokenDB = new HashMap[String,IdAndName]()
  // add some default data
  tokenDB += ("4512a806047e7e7d9356cebe5eb3eaf2" -> IdAndName(1000001L,"xueliu"))
  tokenDB += ("94cde55959dc152e9185626925d33329" -> IdAndName(1000002L,"yazhang"))

  def generateToken(u:String, p:String) = {
    userDB_by_name.get(u) match {
      case Some(id) => {
        userDB_by_id.get(id) match {
          case Some(nameAndHash) => {
            val hash = md5(u+p)
            hash == nameAndHash._2 match {
              case true => {
                // use hash as token, have to modify later
                // token also needs a expiration date
                if(!tokenDB.contains(hash)){
                  tokenDB += (hash -> IdAndName(id,u))
                }
                TokenAndId(hash,id)
              }
              case false => Error("password does not match")
            }
          }
          case None => Error("unconsistent data in server")
        }

      }
      case None => Error("username not found")
    }
  }

  def addUser(u:String, p:String) = {
    userDB_by_name.get(u) match {
      case Some(_) => Error("user exists")
      case None => {
        val new_id = id_now
        id_now += 1
        userDB_by_name.put(u,new_id)
        userDB_by_id.put(new_id,(u,md5(u+p)))
        userPageDB.put(new_id,new ListBuffer[Long])
        ID(new_id)
      }
    }
  }

  def receive = {
    case UserPassRequest(t:String, u:String,p:String) => {
      t match {
        case "getToken" => sender ! generateToken(u, p)
        case "register" => sender ! addUser(u, p)
        case _ => sender ! Error("unsupported userPass request.")
      }
    }
    case Token(token) => {
      tokenDB.get(token) match {
        case Some(id_name) => {
          sender ! id_name
        }
        case None => {
          sender ! Error("invalid token.")
        }
      }
    }
    case FeedRequest(id,from,to) => {
      userPageDB.get(id) match {
        case Some(data) => sender ! Timeline(data.slice(from,to).toArray,"prev","next")
        case None => sender ! Error("invalid username")
      }
    }
    case RequestIdId(req,pid,uids) => {
      req match {
        case "addPost" => {
          for (i <- 0 until uids.ids.length){
            pid +=: userPageDB(uids.ids(i))
          }
          sender ! OK("adding post done")
        }
        case _ => sender ! Error("unsupported userActor request.")
      }
    }
    case _ => sender ! Error("unsupported userActor request.")
  }
}
