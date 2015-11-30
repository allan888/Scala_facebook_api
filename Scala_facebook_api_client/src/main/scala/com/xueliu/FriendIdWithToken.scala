package com.xueliu

import spray.json.DefaultJsonProtocol

object FriendIdWithTokenJsonProtocol extends DefaultJsonProtocol {

  implicit val friendidwithtokenFormat = jsonFormat2(FriendIdWithToken)
}
case class FriendIdWithToken(friend_id: Long, access_token:String)