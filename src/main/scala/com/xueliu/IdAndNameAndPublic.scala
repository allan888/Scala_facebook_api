package com.xueliu

import spray.json.DefaultJsonProtocol
object IdAndNameAndPublicJsonProtocol extends DefaultJsonProtocol {

  implicit val idandnameandpublicFormat = jsonFormat3(IdAndNameAndPublic)
}
case class IdAndNameAndPublic(id:Long,name:String,public:String)