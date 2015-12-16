package com.xueliu

import spray.json.DefaultJsonProtocol

object Phase1JsonProtocol extends DefaultJsonProtocol {

  implicit val phase1Format = jsonFormat2(Phase1)
}

case class Phase1(username: String, clientPublicKey:String) {}