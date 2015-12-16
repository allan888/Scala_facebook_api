package com.xueliu

import IdAndNameAndPublicJsonProtocol._
import spray.json.DefaultJsonProtocol
object IdAndNameAndPublicArrayJsonProtocol extends DefaultJsonProtocol {

  implicit val idandnameandpublicarrayFormat = jsonFormat1(IdAndNameAndPublicArray)
}
case class IdAndNameAndPublicArray(data:Array[IdAndNameAndPublic])