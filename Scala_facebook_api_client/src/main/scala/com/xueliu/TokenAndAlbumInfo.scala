package com.xueliu
import spray.json.DefaultJsonProtocol
object TokenAndAlbumInfoJsonProtocol extends DefaultJsonProtocol {

  implicit val tokenandalbuminfoFormat = jsonFormat2(TokenAndAlbumInfo)
}
case class TokenAndAlbumInfo(access_token:String, album_name: String)
