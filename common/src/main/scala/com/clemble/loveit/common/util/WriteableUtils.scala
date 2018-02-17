package com.clemble.loveit.common.util

import akka.util.ByteString
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.{Format, JsArray}

object WriteableUtils {

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

  implicit def jsonCollectionToWriteable[T]()(implicit jsonFormat: Format[T]): Writeable[List[T]] = new Writeable[List[T]]((l: List[T]) => {
    val json = JsArray(l.map(jsonFormat.writes))
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))


}
