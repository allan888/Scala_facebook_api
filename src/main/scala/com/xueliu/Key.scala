package com.xueliu

import spray.json.DefaultJsonProtocol
object KeyJsonProtocol extends DefaultJsonProtocol {

  implicit val keyFormat = jsonFormat1(Key)
}
case class Key(key: String)