package com.xueliu
import spray.json.DefaultJsonProtocol
object FeedRequestJsonProtocol extends DefaultJsonProtocol {

  implicit val feedrequestFormat = jsonFormat3(FeedRequest)
}
case class FeedRequest(username:Long,from:Int,to:Int)