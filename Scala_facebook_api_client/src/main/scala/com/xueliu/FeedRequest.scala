package com.xueliu
import spray.json.DefaultJsonProtocol
object FeedRequestJsonProtocol extends DefaultJsonProtocol {

  implicit val feedrequestFormat = jsonFormat4(FeedRequest)
}
case class FeedRequest(typ:String,username:Long,from:Int,to:Int)