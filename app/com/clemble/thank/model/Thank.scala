package com.clemble.thank.model

import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  url: String,
                  given: Amount
                )

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

}