package com.xueliu
import spray.json.DefaultJsonProtocol
import IDArrayJsonProtocol._
object RequestIdIdJsonProtocol extends DefaultJsonProtocol {

  implicit val requestididFormat = jsonFormat3(RequestIdId)
}
case class RequestIdId(request: String,id1:Long,id2:IDArray)