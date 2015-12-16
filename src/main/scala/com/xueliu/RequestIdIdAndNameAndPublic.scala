package com.xueliu

import spray.json.DefaultJsonProtocol
import IdAndNameAndPublicJsonProtocol._
object RequestIdIdAndNameAndPublicJsonProtocol extends DefaultJsonProtocol {

  implicit val requestididandnameandpublicFormat = jsonFormat3(RequestIdIdAndNameAndPublic)
}
case class RequestIdIdAndNameAndPublic(request: String,id1:Long,id_name_pub:IdAndNameAndPublic)