package com.xueliu

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
        parameters('access_token, 'otherPara.?) { (token, otherPara) =>

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
                    val future = userActorSelection ? FeedRequest(id, 0, 10)
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

    } ~ post {
      // 这里面的请求是 POST 的
      path("getToken" /) {
        entity(as[UserPass]) {
          info => {
            complete {
              info.getToken(userActorSelection).toString
            }
          }
        }
      } ~ path("register" /) {
        entity(as[UserPass]) {
          info => {
            complete {
              info.register(userActorSelection).toString
            }
          }
        }
      } ~ path(LongNumber / "feed" /) { l_id =>
        entity(as[MessageWithToken]) {
          msg => {
            complete {
              MessageWithTokenAndId(msg.message, msg.access_token, l_id).postMessage(userActorSelection, contentActorSelection, friendsListActorSelection) match {
                case x: ID => x.toJson.toString
                case x: Error => x.toJson.toString
                case _ => Error("internal error").toJson.toString
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