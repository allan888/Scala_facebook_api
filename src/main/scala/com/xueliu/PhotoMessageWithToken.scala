package com.xueliu

import spray.json.DefaultJsonProtocol

object PhotoMessageWithTokenJsonProtocol extends DefaultJsonProtocol {

  implicit val photomessagewithtokenFormat = jsonFormat4(PhotoMessageWithToken)
}
case class PhotoMessageWithToken(message: String, photoData:String,albumId:Long,access_token:String)