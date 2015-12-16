package com.xueliu

import spray.json.DefaultJsonProtocol

object Phase2JsonProtocol extends DefaultJsonProtocol {

  implicit val phase2Format = jsonFormat2(Phase2)
}

case class Phase2(username: String, sig:String) {}