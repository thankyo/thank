package com.clemble.loveit.auth.model

import java.time.LocalDateTime
import java.util.UUID

import com.clemble.loveit.common.model.{CreatedAware, UserID}
import com.clemble.loveit.user.model.UserAware
import play.api.libs.json.{Json, OFormat}

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param token The unique token ID.
 * @param user The unique ID of the user the token is associated with.
 */
case class AuthToken(
                      token: UUID,
                      user: UserID,
                      created: LocalDateTime = LocalDateTime.now()
) extends CreatedAware with UserAware

object AuthToken {

  implicit val jsonFormat: OFormat[AuthToken] = Json.format[AuthToken]

}
