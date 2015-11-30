package com.xueliu

import spray.json.DefaultJsonProtocol
import IDArrayJsonProtocol._
object RequestPhotoInfoIdJsonProtocol extends DefaultJsonProtocol {

  implicit val requestphotoinfoidFormat = jsonFormat4(RequestPhotoInfoId)
}
case class RequestPhotoInfoId(request: String,photoId:Long,albumId:Long,ids:IDArray)