package com.xueliu
import spray.json.DefaultJsonProtocol
object ErrorJsonProtocol extends DefaultJsonProtocol {

  implicit val errorFormat = jsonFormat1(Error)
}
case class Error(error: String)