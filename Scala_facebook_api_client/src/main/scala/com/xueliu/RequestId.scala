package com.xueliu
import spray.json.DefaultJsonProtocol
object RequestIdJsonProtocol extends DefaultJsonProtocol {

  implicit val requestidFormat = jsonFormat2(RequestId)
}
case class RequestId(request: String,id:Long)