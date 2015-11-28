package com.xueliu

import spray.json.DefaultJsonProtocol
import IdAndNameJsonProtocol._
object RequestIdIdAndNameJsonProtocol extends DefaultJsonProtocol {

  implicit val requestididandnameFormat = jsonFormat3(RequestIdIdAndName)
}
case class RequestIdIdAndName(request: String,id1:Long,id_name:IdAndName)