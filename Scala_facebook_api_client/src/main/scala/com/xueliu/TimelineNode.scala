package com.xueliu

import spray.json.DefaultJsonProtocol
object TimelineNodeJsonProtocol extends DefaultJsonProtocol {

  implicit val timelinenodeFormat = jsonFormat3(TimelineNode)
}
case class TimelineNode(data:Array[Map[String,String]], previous:Int, next:Int) {

}