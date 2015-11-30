package com.xueliu

import spray.json.DefaultJsonProtocol
import PersonInfoJsonProtocol._

object TokenAndPersonInfoJsonProtocol extends DefaultJsonProtocol {

  implicit val tokenandpersoninfoFormat = jsonFormat2(TokenAndPersonInfo)
}
case class TokenAndPersonInfo(access_token: String,personInfo: PersonInfo)