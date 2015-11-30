package com.xueliu

import java.util.Calendar

import akka.actor.{Props, Actor}
import akka.util.Timeout
import spray.routing.PathMatchers.Segment
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import ErrorJsonProtocol._
import UserPassJsonProtocol._
import TokenJsonProtocol._
import TimelineJsonProtocol._
import IDJsonProtocol._
import MessageWithTokenJsonProtocol._
import spray.json._
import akka.pattern.ask
import scala.concurrent.duration._
import TimelineNodeJsonProtocol._
import OKJsonProtocol._
import TokenAndIdJsonProtocol._
import IdAndNameArrayJsonProtocol._
import DefaultJsonProtocol._
import FriendIdWithTokenJsonProtocol._
import PersonInfoJsonProtocol._
import TokenAndPersonInfoJsonProtocol._
import PhotoMessageWithTokenJsonProtocol._
import PostNoIdJsonProtocol._
import IDArrayJsonProtocol._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class FacebookGoActor extends Actor with FacebookService {


  //val userActor = context.actorOf(Props[UserActor], "userActor")
  val userActorSelection = context.actorSelection("akka.tcp://facebook@127.0.0.1:8082/user/userService")
  val contentActorSelection = context.actorSelection("akka.tcp://facebook@127.0.0.1:8082/user/contentService")
  val friendsListActorSelection = context.actorSelection("akka.tcp://facebook@127.0.0.1:8082/user/friendsListService")
  val myRoute = {

    // differentiate get and post first
    // all actions should return a Json
    // 区分出请求,这里是GET的
    get {
      // 网站的根目录
      path("") {
        complete {
          "get -> root path"
        }
      } ~ path("me" / "feed" /) {
        // 这里面是网站的/me/feed/目录

        // 下面这行时说token是必须的, otherPara后面有问号是说otherPara是可选的
        parameters('access_token, 'from ? 0, 'num ? 10, 'otherPara.?) { (token, from, num, otherPara) =>
          respondWithMediaType(`application/json`) {
            complete {
              // 判断一下otherPara有没有传进来
              otherPara match {

                case Some(otherStr) => {
                  // 如果有otherPara的话,如何如何
                  "test"
                }
                case None => {
                  //这边是如果没有otherPara的话如何如何

                  //Token(token.toString)是实例化了一个新的Token对象
                  //在这个新对象上调用getUsername,来把token转换成对应的ID
                  Token(token).getUserIdAndName(userActorSelection) match {

                    //error的话如何如何
                    case err: Error => err.toJson.toString

                    //如果转换用户名成功的话,如何如何
                    case IdAndName(id, name) => {
                      implicit val timeout = Timeout(10 seconds)
                      val future = userActorSelection ? FeedRequest("timeline", id, from, num)
                      val ini = Await.result(future, Duration.Inf)
                      ini match {
                        case x: Timeline => x.toNodeFormat(contentActorSelection) match {
                          case x: TimelineNode => x.toJson.toString
                          case x: Error => {
                            println(x.error)
                            x.toJson.toString
                          }
                          case _ => Error("internal error").toJson.toString
                        }
                        case x: Error => {
                          println(x.error)
                          x.toJson.toString
                        }
                        case _ => Error("internal error").toJson.toString
                      }
                    }

                    case _ => Error("internal error").toJson.toString
                  }
                }
              }

            }
          }
        }
      } ~ path(LongNumber / "feed" /) { l_id =>

        parameters('access_token, 'from ? 0, 'num ? 10, 'otherPara.?) { (token, from, num, otherPara) =>
          respondWithMediaType(`application/json`) {
            complete {
              otherPara match {

                case Some(otherStr) => {
                  "test"
                }
                case None => {
                  Token(token).getUserIdAndName(userActorSelection) match {

                    //error的话如何如何
                    case err: Error => err.toJson.toString

                    //如果转换用户名成功的话,如何如何
                    case IdAndName(id, name) => {
                      implicit val timeout = Timeout(10 seconds)
                      // 获取网址里面传进来的id的对应的人的timeline
                      val future = userActorSelection ? FeedRequest("own", l_id, from, num)
                      val ini = Await.result(future, Duration.Inf)
                      ini match {
                        case x: Timeline => x.toNodeFormat(contentActorSelection) match {
                          case x: TimelineNode => x.toJson.toString
                          case x: Error => x.toJson.toString
                          case _ => Error("internal error").toJson.toString
                        }
                        case x: Error => x.toJson.toString
                        case _ => Error("internal error").toJson.toString
                      }
                    }

                    case _ => Error("internal error").toJson.toString
                  }
                }
              }

            }
          }
        }
      } ~ path(LongNumber / "friends" /) { l_id =>

        parameters('access_token, 'otherPara.?) { (token, otherPara) =>
          respondWithMediaType(`application/json`) {
            complete {
              otherPara match {

                case Some(otherStr) => {
                  "test"
                }
                case None => {
                  Token(token).getUserIdAndName(userActorSelection) match {
                    case err: Error => err.toJson.toString

                    case IdAndName(id, name) => {
                      implicit val timeout = Timeout(10 seconds)
                      val future = friendsListActorSelection ? RequestId("get", l_id)
                      val ini = Await.result(future, Duration.Inf)
                      ini match {
                        case x: IdAndNameArray => x.toJson.toString
                        case x: Error => x.toJson.toString
                        case _ => Error("internal error").toJson.toString
                      }
                    }

                    case _ => Error("internal error").toJson.toString
                  }
                }
              }

            }
          }
        }
      } ~ path("me" /) {
        parameters('access_token, 'fields) { (token, fields) =>
          respondWithMediaType(`application/json`) {
            complete {
              Token(token).getUserIdAndName(userActorSelection) match {

                case err: Error => err.toJson.toString

                case IdAndName(id, name) => {
                  implicit val timeout = Timeout(10 seconds)
                  val future = userActorSelection ? RequestId("getUserInfo", id)
                  val ini = Await.result(future, Duration.Inf)
                  ini match {
                    case x: PersonInfo => x.toJson.toString
                    case x: Error => {
                      println(x.error)
                      x.toJson.toString
                    }
                    case _ => Error("internal error").toJson.toString
                  }
                }

                case _ => Error("internal error").toJson.toString
              }
            }
          }
        }
      } ~ path(LongNumber /) { l_id =>
        parameters('access_token) { (token) =>
          respondWithMediaType(`application/json`) {
            complete {
              Token(token).getUserIdAndName(userActorSelection) match {

                case err: Error => err.toJson.toString

                case IdAndName(id, name) => {
                  if(l_id < 5000000L){ // user id
                    implicit val timeout = Timeout(10 seconds)
                    val future = userActorSelection ? RequestId("getUserInfo", l_id)
                    val ini = Await.result(future, Duration.Inf)
                    ini match {
                      case x: PersonInfo => x.toJson.toString
                      case x: Error => x.toJson.toString
                      case _ => Error("internal error").toJson.toString
                    }
                  }else if(l_id < 9000000L){ // post id
                    implicit val timeout = Timeout(10 seconds)
                    val future = contentActorSelection ? ID(l_id)
                    val ini = Await.result(future, Duration.Inf)
                    ini match {
                      case x: PostNoId => x.toJson.toString
                      case x: Error => x.toJson.toString
                      case _ => Error("internal error").toJson.toString
                    }
                  }else if(l_id < 9999999L){ // album id
                    implicit val timeout = Timeout(10 seconds)
                    val future = userActorSelection ? RequestId("getAlbumList",l_id)
                    val ini = Await.result(future, Duration.Inf)
                    import AlbumJsonProtocol._
                    ini match {
                      case x: Album => x.toJson.toString
                      case x: Error => x.toJson.toString
                      case _ => Error("internal error").toJson.toString
                    }
                  }else{
                    Error("invalid node id").toJson.toString
                  }
                }

                case _ => Error("internal error").toJson.toString
              }
            }
          }
        }
      }

    } ~ post {
      // 这里面的请求是 POST 的
      path("getToken" /) {
        entity(as[UserPass]) {
          info => {
            respondWithMediaType(`application/json`) {
              complete {
                info.getToken(userActorSelection) match {
                  case x: TokenAndId => x.toJson.toString
                  case x: Error => x.toJson.toString
                  case _ => Error("internal error").toJson.toString
                }
              }
            }
          }
        }
      } ~ path("register" /) {
        respondWithMediaType(`application/json`) {
          entity(as[UserPass]) {
            info => {
              respondWithMediaType(`application/json`) {
                complete {
                  info.register(userActorSelection, friendsListActorSelection) match {
                    case x: ID => {
                      import DefaultJsonProtocol._
                      x.toJson.toString
                    }
                    case x: Error => x.toJson.toString
                    case _ => Error("internal error").toJson.toString
                  }
                }
              }
            }
          }
        }
      } ~ path(LongNumber / "feed" /) { l_id =>  // user id -> feed, post a new message to (user)'s page
        entity(as[PhotoMessageWithToken]) {
          msg => {
            respondWithMediaType(`application/json`) {
              complete {
                MessageWithTokenAndId(msg.message, "photo", msg.photoData, msg.albumId, msg.access_token, l_id).postMessage(userActorSelection, contentActorSelection, friendsListActorSelection) match {
                  case x: ID => x.toJson.toString
                  case x: Error => x.toJson.toString
                  case _ => Error("internal error").toJson.toString
                }
              }
            }
          }
        } ~ entity(as[MessageWithToken]) {
          msg => {
            respondWithMediaType(`application/json`) {
              complete {
                println("message post")
                MessageWithTokenAndId(msg.message, "post", "", 0L, msg.access_token, l_id).postMessage(userActorSelection, contentActorSelection, friendsListActorSelection) match {
                  case x: ID => x.toJson.toString
                  case x: Error => {
                    println(x.error)
                    x.toJson.toString
                  }
                  case _ => Error("internal error").toJson.toString
                }
              }
            }
          }
        }
      } ~ path(LongNumber / "friends" /) { l_id => // user id -> get friend
        entity(as[FriendIdWithToken]) {
          f_id_token => {
            respondWithMediaType(`application/json`) {
              complete {
                Token(f_id_token.access_token).getUserIdAndName(userActorSelection) match {
                  case id_name: IdAndName => {
                    id_name.id == l_id match {
                      // id_name.id = me
                      case true => {
                        implicit val timeout = Timeout(10 seconds)
                        val future1 = userActorSelection ? RequestId("getUsername", f_id_token.friend_id)
                        val ini1 = Await.result(future1, Duration.Inf)
                        ini1 match {
                          case x: IdAndName => {
                            val future2 = friendsListActorSelection ? RequestIdIdAndName("add", l_id, x)
                            val ini2 = Await.result(future2, Duration.Inf)
                            ini2 match {
                              case y: OK => {
                                val future3 = friendsListActorSelection ? RequestIdIdAndName("add", x.id, id_name)
                                val ini3 = Await.result(future3, Duration.Inf)
                                ini2 match {
                                  case z: OK => z.toJson.toString
                                  case z: Error => {
                                    println(z.error)
                                    z.toJson.toString
                                  }
                                  case _ => Error("internal error").toJson.toString
                                }
                              }
                              case y: Error => {
                                println(y.error)
                                y.toJson.toString
                              }
                              case _ => Error("internal error").toJson.toString
                            }
                          }
                          case _ => Error("internal error").toJson.toString
                        }
                      }
                      case _ => Error("token does not match user id").toJson.toString
                    }
                  }
                  case _ => Error("invalid token").toJson.toString
                }
              }
            }

          }
        }
      } ~ path("me" /) {
        entity(as[TokenAndPersonInfo]) {
          t_info => {
            respondWithMediaType(`application/json`) {
              complete {
                implicit val timeout = Timeout(10 seconds)
                val future1 = userActorSelection ? t_info
                val ini1 = Await.result(future1, Duration.Inf)
                ini1 match {
                  case x: OK => x.toJson.toString
                  case x: Error => x.toJson.toString
                  case _ => Error("internal error").toJson.toString
                }
              }
            }
          }
        }
      } ~ path("album" /) {
        import TokenAndAlbumInfoJsonProtocol._
        entity(as[TokenAndAlbumInfo]) {
          t_info => {
            respondWithMediaType(`application/json`) {
              complete {
                implicit val timeout = Timeout(10 seconds)
                val future1 = userActorSelection ? ("createAlbum",t_info)
                val ini1 = Await.result(future1, Duration.Inf)
                ini1 match {
                  case x: ID => x.toJson.toString
                  case x: Error => x.toJson.toString
                  case _ => Error("internal error").toJson.toString
                }
              }
            }
          }
        }
      } ~ path(LongNumber /) { l_num =>// any node is, could be person, album or post, this means update this node's info
        import TokenAndAlbumInfoJsonProtocol._
        entity(as[TokenAndPersonInfo]) {
          t_info => {
            respondWithMediaType(`application/json`) {
              complete {
                if(l_num >1000000 && l_num < 5000000L){
                  Token(t_info.access_token).getUserIdAndName(userActorSelection) match{
                    case user_info:IdAndName => {
                      if(user_info.id == l_num){
                        implicit val timeout = Timeout(10 seconds)
                        val future1 = userActorSelection ? t_info
                        val ini1 = Await.result(future1, Duration.Inf)
                        ini1 match {
                          case x: OK => x.toJson.toString
                          case x: Error => x.toJson.toString
                          case _ => Error("internal error").toJson.toString
                        }
                      }else{
                        Error("invalid token").toJson.toString
                      }
                    }
                    case _ => Error("invalid token").toJson.toString
                  }
                }else{
                  Error("node number should be a person's id if you want to update profile").toJson.toString
                }
              }
            }
          }
        } ~ entity(as[TokenAndAlbumInfo]) {
          a_info => {
            respondWithMediaType(`application/json`) {
              complete {
                if(l_num >9000000 && l_num < 9999999L){
                  Token(a_info.access_token).getUserIdAndName(userActorSelection) match{
                    case user_info:IdAndName => {
                      implicit val timeout = Timeout(10 seconds)
                      val future = userActorSelection ? ("updateAlbum", user_info.id, l_num, a_info.album_name)
                      val ini = Await.result(future, Duration.Inf)
                      ini match {
                        case x:OK => x.toJson.toString
                        case x:Error => x.toJson.toString
                        case _ => Error("internal error").toJson.toString
                      }
                    }
                    case _ => Error("invalid token").toJson.toString
                  }
                }else{
                  Error("node number should be a album's id if you want to update album infomation").toJson.toString
                }
              }
            }
          }
        }
      }
    } ~ delete {
      path(LongNumber /) {
        l_num =>
          parameters('access_token) {
            (token) =>
              respondWithMediaType(`application/json`) {
                complete {
                  if(l_num < 5000000L) {
                    // friend id
                    Token(token).getUserIdAndName(userActorSelection) match {
                      case userIdAndName:IdAndName =>{
                        implicit val timeout = Timeout(10 seconds)
                        val future1 = friendsListActorSelection ? RequestIdIdAndName("del", l_num,userIdAndName)
                        val ini1 = Await.result(future1, Duration.Inf)
                        ini1 match {
                          case x: OK => x.toJson.toString
                          case x: Error => {
                            println(x.error)
                            x.toJson.toString
                          }
                          case _ => Error("internal error").toJson.toString
                        }
                      }
                      case x => Error("invalid token").toJson.toString
                    }
                  }else if(l_num < 9000000L){
                    // delete post
                    Token(token).getUserIdAndName(userActorSelection) match {
                      case userIdAndName:IdAndName =>{
                        implicit val timeout = Timeout(10 seconds)
                        val future1 = contentActorSelection ? ("delete", userIdAndName.id, l_num)
                        val ini1 = Await.result(future1, Duration.Inf)
                        ini1 match {
                          case x: OK => x.toJson.toString
                          case x: Error => {
                            println(x.error)
                            x.toJson.toString
                          }
                          case _ => Error("internal error").toJson.toString
                        }
                      }
                      case x => Error("invalid token").toJson.toString
                    }


                  }else if(l_num < 9999999L){
                    // album id
                    Token(token).getUserIdAndName(userActorSelection) match {
                      case userIdAndName:IdAndName =>{
                        implicit val timeout = Timeout(10 seconds)
                        val future1 = userActorSelection ? ("deleteAlbum", userIdAndName.id, l_num)
                        val ini1 = Await.result(future1, Duration.Inf)
                        ini1 match {
                          case x: OK => x.toJson.toString
                          case x: Error => {
                            println(x.error)
                            x.toJson.toString
                          }
                          case _ => Error("internal error").toJson.toString
                        }
                      }
                      case x => Error("invalid token").toJson.toString
                    }


                  }else{
                    Error("invalid node number").toJson.toString
                  }
                }
              }
          }
      }
    }
  }

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait FacebookService extends HttpService {

}

/*
publish a new post:
POST graph.facebook.com
/{user-id}/feed?
message={message}&
access_token={access-token}

*/
