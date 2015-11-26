package com.xueliu
import spray.json.DefaultJsonProtocol
object IDJsonProtocol extends DefaultJsonProtocol {

  implicit val idFormat = jsonFormat1(ID)
}
case class ID(id:Long)