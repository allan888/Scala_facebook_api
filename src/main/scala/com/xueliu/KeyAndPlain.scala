package com.xueliu

import spray.json.DefaultJsonProtocol
object KeyAndPlainJsonProtocol extends DefaultJsonProtocol {

  implicit val keyandplainFormat = jsonFormat2(KeyAndPlain)
}
case class KeyAndPlain(key: String,plain:String)