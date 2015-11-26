package com.xueliu
import spray.json.DefaultJsonProtocol
object OKJsonProtocol extends DefaultJsonProtocol {

  implicit val okFormat = jsonFormat1(OK)
}
case class OK(ok: String)