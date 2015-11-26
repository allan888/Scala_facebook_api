package com.xueliu
import spray.json.DefaultJsonProtocol
object IdAndNameJsonProtocol extends DefaultJsonProtocol {

  implicit val idandnameFormat = jsonFormat2(IdAndName)
}
case class IdAndName(id:Long,name:String)