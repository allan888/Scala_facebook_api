package com.xueliu
import spray.json.DefaultJsonProtocol
import java.util.Date
object PostNoIdJsonProtocol extends DefaultJsonProtocol {

  implicit val postnoidFormat = jsonFormat7(PostNoId)
}
case class PostNoId(userid:Long,username:String,typ:String, date:String, content:String,aux:String, aux2:Long)