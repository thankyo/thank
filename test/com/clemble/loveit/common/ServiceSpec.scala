package com.clemble.loveit.common

import com.clemble.loveit.auth.controller.SocialAuthController
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{OwnedProject, Project, Resource, User, UserID, Verification}
import com.clemble.loveit.payment.service.UserPaymentService
import com.clemble.loveit.thank.service.{PostService, ProjectService, UserProjectsService}
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.ExecutionContext.Implicits.global

trait ServiceSpec extends FunctionalThankSpecification {

  lazy val authController: SocialAuthController = dependency[SocialAuthController]
  lazy val userService: UserService = dependency[UserService]
  lazy val userPayService: UserPaymentService = dependency[UserPaymentService]
  lazy val userPrjService: UserProjectsService = dependency[UserProjectsService]
  lazy val userRep: UserRepository = dependency[UserRepository]
  lazy val prjRepo: ProjectRepository = dependency[ProjectRepository]
  lazy val prjService: ProjectService = dependency[ProjectService]
  lazy val postService: PostService = dependency[PostService]

  override def createUser(register: RegistrationRequest = someRandom[RegistrationRequest]): UserID = {
    val fUserID = for {
      user <- userService.create(register.toUser())
      _ <- userPayService.create(user)
      _ <- userPrjService.create(user)
    } yield {
      user.id
    }
    await(fUserID)
  }

  override def getUser(user: UserID): Option[User] = {
    await(userService.findById(user))
  }

  override def createProject(user: UserID = createUser(), url: Resource = randomResource): Project = {
    val ownedProject = await(userPrjService.dibsOnUrl(user, url))
    await(prjService.create(user, ownedProject))
  }

}
