package com.xueliu
import spray.json.DefaultJsonProtocol
object PersonInfoJsonProtocol extends DefaultJsonProtocol {

  implicit val personinfoFormat = jsonFormat5(PersonInfo)
}
case class PersonInfo(name: String, birthday:String,gender:String,livePlace:String,birthPlace:String)