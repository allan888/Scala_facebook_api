package com.xueliu
import spray.json.DefaultJsonProtocol
import IDArrayJsonProtocol._
object AlbumJsonProtocol extends DefaultJsonProtocol {

  implicit val albumFormat = jsonFormat2(Album)
}
case class Album(album_name:String,photo_list: Array[Long])
