package com.xueliu
import spray.json.DefaultJsonProtocol
object IDArrayJsonProtocol extends DefaultJsonProtocol {

  implicit val idarrayFormat = jsonFormat1(IDArray)
}
case class IDArray(ids:Array[Long])