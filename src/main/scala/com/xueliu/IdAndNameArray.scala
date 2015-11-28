package com.xueliu
import IdAndNameJsonProtocol._
import spray.json.DefaultJsonProtocol
object IdAndNameArrayJsonProtocol extends DefaultJsonProtocol {

  implicit val idandnamearrayFormat = jsonFormat1(IdAndNameArray)
}
case class IdAndNameArray(data:Array[IdAndName])