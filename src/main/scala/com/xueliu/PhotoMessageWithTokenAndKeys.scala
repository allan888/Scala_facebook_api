package com.xueliu

import spray.json.DefaultJsonProtocol

object PhotoMessageWithTokenAndKeysJsonProtocol extends DefaultJsonProtocol {

  implicit val photomessagewithtokenandkeysFormat = jsonFormat5(PhotoMessageWithTokenAndKeys)
}
case class PhotoMessageWithTokenAndKeys(message: String, photoData:String,albumId:Long,access_token:String,keys:Map[Long,String])