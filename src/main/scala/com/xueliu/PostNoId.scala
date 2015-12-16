package com.xueliu
import scala.collection.mutable.HashMap
import spray.json.DefaultJsonProtocol
import java.util.Date



object PostNoIdJsonProtocol extends DefaultJsonProtocol {

  implicit val postnoidFormat = jsonFormat8(PostNoId)
}
case class PostNoId(userid:Long,username:String,typ:String, date:String, content:String,aux:String, aux2:Long, keys:Map[String, String])