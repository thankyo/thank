package com.clemble.loveit.auth.controller

import java.util.UUID
import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.clemble.loveit.auth.model.requests.RestorePasswordRequest
import com.clemble.loveit.auth.service.{AuthTokenService, UserLoggedIn}
import com.clemble.loveit.common.error.FieldValidationError
import play.api.i18n.I18nSupport
import play.api.libs.json.JsBoolean
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * The `Reset Password` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param authTokenService       The auth token service implementation.
 * @param ex                     The execution context.
 */
class ResetPasswordController @Inject()(
                                          components: ControllerComponents,
                                          userService: UserService,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry,
                                          authTokenService: AuthTokenService
)(
  implicit
  silhouette: Silhouette[AuthEnv],
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Resets the password.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def submit(token: UUID) = silhouette.UnsecuredAction.async(parse.json[RestorePasswordRequest]) { implicit request =>
    val passwordInfo = passwordHasherRegistry.current.hash(request.body.password)
    for {
      authTokenOpt <- authTokenService.validate(token)
      authToken = authTokenOpt.getOrElse({ throw FieldValidationError("password", "Token expired or already used") })
      userOpt <- userService.findById(authToken.user)
      loginInfoOpt = userOpt.flatMap(_.profiles.find(_.providerID == CredentialsProvider.ID))
      _ <- authInfoRepository.update[PasswordInfo](loginInfoOpt.get, passwordInfo)
      authResult <- AuthUtils.authResponse(UserLoggedIn(userOpt.get, loginInfoOpt.get))
    } yield {
      authResult
    }
  }

}