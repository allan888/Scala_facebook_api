package com.xueliu

import akka.actor._
import scala.collection.mutable.{ListBuffer, HashMap}
import java.security.MessageDigest

import scala.collection.mutable

/**
  * Created by xueliu on 11/21/15.
  */
class UserActor() extends Actor {
  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }

  var id_now: Long = 1000003L
  var album_now: Long = 9000003L

  // nodeID -> (username, md5(password) )
  val userDB_by_id = new HashMap[Long, (String, String, ListBuffer[Long], PersonInfo)]()
  // add some default users, data has not been persisted.
  userDB_by_id += (1000001L ->("xueliu", md5("xueliu" + "123456"), new ListBuffer[Long]() += 9000001L, PersonInfo("xueliu", "1989-06-06", "female", "Gainesville,FL", "China")))
  userDB_by_id += (1000002L ->("yazhang", md5("yazhang" + "654321"), new ListBuffer[Long]() += 9000002L, PersonInfo("yazhang", "1988-06-06", "female", "Gainesville,FL", "China")))

  val albumDB = new HashMap[Long, ListBuffer[Long]]
  albumDB += (9000001L -> new ListBuffer[Long])
  //albumDB += (9000001L -> (albumDB.get(9000001L).getOrElse(new ListBuffer[Long]) += 1000003L) )
  albumDB += (9000001L -> new ListBuffer[Long])
  albumDB += (9000002L -> new ListBuffer[Long])
  //albumDB += (9000002L -> albumDB.get(9000001L).getOrElse(new ListBuffer[Long]) )

  val album_info = new HashMap[Long, (String, IdAndName)]
  album_info += (9000001L ->("default album", IdAndName(1000001L, "xueliu")))
  album_info += (9000002L ->("default album", IdAndName(1000002L, "yazhang")))


  // nodeID -> (username, md5(password) )
  val userDB_by_name = new HashMap[String, Long]()
  // add some default users, data has not been persisted.
  userDB_by_name += ("xueliu" -> 1000001L)
  userDB_by_name += ("yazhang" -> 1000002L)

  // username -> id,type
  // post id +=: ListBuffer to prepend a new post to user's timeline
  val userPageDB = new mutable.HashMap[Long, ListBuffer[Long]]()

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


  val userOwnDB = new mutable.HashMap[Long, ListBuffer[Long]]()
  userOwnDB += (1000001L -> new ListBuffer[Long])
  5000001L +=: userOwnDB(1000001L)
  5000002L +=: userOwnDB(1000001L)

  userOwnDB += (1000002L -> new ListBuffer[Long])
  5000003L +=: userOwnDB(1000002L)
  5000004L +=: userOwnDB(1000002L)

  // token -> username
  val tokenDB = new HashMap[String, IdAndName]()
  // add some default data
  tokenDB += ("4512a806047e7e7d9356cebe5eb3eaf2" -> IdAndName(1000001L, "xueliu"))
  tokenDB += ("94cde55959dc152e9185626925d33329" -> IdAndName(1000002L, "yazhang"))

  def generateToken(u: String, p: String) = {
    userDB_by_name.get(u) match {
      case Some(id) => {
        userDB_by_id.get(id) match {
          case Some(nameAndHash) => {
            val hash = md5(u + p)
            hash == nameAndHash._2 match {
              case true => {
                // use hash as token, have to modify later
                // token also needs a expiration date
                if (!tokenDB.contains(hash)) {
                  tokenDB += (hash -> IdAndName(id, u))
                }
                TokenAndId(hash, id)
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

  def addUser(u: String, p: String) = {
    userDB_by_name.get(u) match {
      case Some(_) => Error("user exists")
      case None => {
        val new_id = id_now
        id_now += 1
        userDB_by_name.put(u, new_id)
        val new_album_id = album_now
        album_now += 1
        userDB_by_id.put(new_id, (u, md5(u + p), new ListBuffer[Long]() += new_album_id, PersonInfo("", "", "", "", "")))
        albumDB += (new_album_id -> new ListBuffer[Long])
        album_info += (new_album_id ->("default album", IdAndName(new_id, u)))
        userPageDB.put(new_id, new ListBuffer[Long])
        userOwnDB.put(new_id, new ListBuffer[Long])
        ID(new_id)
      }
    }
  }

  def receive = {
    case UserPassRequest(req: String, u: String, p: String) => {
      req match {
        case "getToken" => sender ! generateToken(u, p)
        case "register" => sender ! addUser(u, p)
        case _ => sender ! Error("unsupported userPass request.")
      }
    }
    case TokenAndPersonInfo(t: String, p: PersonInfo) => {
      tokenDB.get(t) match {
        case Some(id_name) => {
          userDB_by_id.get(id_name.id) match {
            case Some(old_info) => {
              userDB_by_id += (id_name.id ->(old_info._1, old_info._2, old_info._3, p))
              sender ! OK("profile updated")
            }
            case None => {
              sender ! Error("user not exists(provided token is valid")
            }
          }
        }
        case None => {
          sender ! Error("invalid token.")
        }
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
    case FeedRequest(typ, id, from, num) => {
      typ match {
        case "timeline" => {
          userPageDB.get(id) match {
            case Some(data) => {

              sender ! Timeline(data.slice(from, from + num).toArray, if (from > 10) (from - 10) else 0, from + num)
            }
            case None => sender ! Error("invalid username")
          }
        }
        case "own" => {
          userOwnDB.get(id) match {
            case Some(data) => {
              sender ! Timeline(data.slice(from, from + num).toArray, if (from > 10) (from - 10) else 0, from + num)
            }
            case None => sender ! Error("invalid username")
          }
        }
        case _ => sender ! Error("invalid type")
      }

    }
    case RequestIdId(req, pid, uids) => {
      req match {
        case "addPost" => {
          // uids is friend list ids
          // the first id in ids contains the poster's id
          pid +=: userOwnDB(uids.ids(0))
          for (i <- 0 until uids.ids.length) {
            pid +=: userPageDB(uids.ids(i))
          }
          sender ! OK("adding post done")
        }
        case _ => sender ! Error("unsupported userActor request.")
      }
    }
    case RequestPhotoInfoId(req, pid, aid, uids) => {
      req match {
        case "addPhoto" => {

          // uids is friend list ids
          // the first id in ids contains the poster's id

          userDB_by_id.get(uids.ids(0)) match {
            case Some(user) => {
              album_info.get(aid) match {
                case Some(info) => {
                  if(info._2.id == uids.ids(0)){
                    albumDB.get(aid) match {
                      case Some(photoList) => {
                        photoList += pid
                        pid +=: userOwnDB(uids.ids(0))
                        for (i <- 0 until uids.ids.length) {
                          pid +=: userPageDB(uids.ids(i))
                        }
                        sender ! OK("adding photo done")
                      }
                      case None => sender ! Error("invalid album id")
                    }
                  }else{
                    sender ! Error("album does not belong to user")
                  }
                }
                case None => {
                  println("album id:",aid)
                  sender ! Error("invalid album id")
                }
              }

            }
            case None => sender ! Error("invalid photo owner id")
          }
        }
        case _ => sender ! Error("unsupported userActor request.")
      }
    }
    case RequestId(req, id) => {
      req match {
        case "getUsername" => {
          userDB_by_id.get(id) match {
            case Some(x) => sender ! IdAndName(id, x._1)
            case None => sender ! Error("user not exists")
          }
        }
        case "getAlbumList" => {
          albumDB.get(id) match {
            case Some(photoList) => {
              album_info.get(id) match{
                case Some(a_info) => {
                  sender ! Album(a_info._1, photoList.toArray)
                }
                case None => sender ! Error("album not exists")
              }
            }
            case None => sender ! Error("album not exists")
          }
        }
        case "getUserInfo" => {
          userDB_by_id.get(id) match {
            case Some(x) => sender ! x._4
            case None => sender ! Error("user not exists")
          }
        }
        case _ => sender ! Error("unsupported userActor request.")
      }
    }
    case ("createAlbum", info: TokenAndAlbumInfo) => {
      tokenDB.get(info.access_token) match {
        case Some(id_name) => {
          userDB_by_id.get(id_name.id) match {
            case Some(x) => {
              val new_album_id = album_now
              album_now += 1
              x._3 += new_album_id
              album_info += (new_album_id ->(info.album_name, IdAndName(id_name.id, x._1)))
              albumDB += (new_album_id -> new ListBuffer[Long])
              sender ! ID(new_album_id)
            }
            case None => sender ! Error("user not exists")
          }
        }
        case None => Error("invalid token")
      }

    }
    case ("deleteAlbum", uid: Long, a_id: Long) => {
      album_info.get(a_id) match{
        case Some(a_info) => {
          if(a_info._2.id == uid){
            userDB_by_id.get(uid) match {
              case Some(user_info) =>{
                if(user_info._3.head != a_id){
                  album_info -= a_id
                  albumDB -= a_id
                  user_info._3 -= a_id
                  sender ! OK("deletion done")
                }else{
                  sender ! Error("you cannot delete default album")
                }
              }
              case None => sender ! Error("fatal error, inconsistency data in userActor")
            }
          }else{
            sender ! Error("album does not belong to user")
          }
        }
        case None => sender ! Error("album info dose not exists")
      }
    }
    case ("getDefaultAlbumId", id: Long) => {
      userDB_by_id.get(id) match {
        case Some(x) => {
          sender ! ID(x._3.head)
        }
        case None => sender ! Error("user does not exists")
      }
    }
    case ("updateAlbum", u_id: Long,a_id:Long,a_name:String) => {
      album_info.get(a_id) match {
        case Some(x) => {
          if(x._2.id == u_id){
            album_info += (a_id -> (a_name, x._2))
            sender ! OK("update album finished")
          }else{
            sender ! Error("album does belong to specified user")
          }
        }
        case None => sender ! Error("album does not exists")
      }
    }
    case _ => sender ! Error("unsupported userActor request.")
  }
  
}
