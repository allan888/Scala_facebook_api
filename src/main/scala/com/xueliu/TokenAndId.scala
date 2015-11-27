package com.xueliu

import spray.json.DefaultJsonProtocol

object TokenAndIdJsonProtocol extends DefaultJsonProtocol {

  implicit val tokenandidFormat = jsonFormat2(TokenAndId)
}
case class TokenAndId(access_token: String,id:Long)