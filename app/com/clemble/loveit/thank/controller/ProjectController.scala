package com.clemble.loveit.thank.controller

import java.util.UUID

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.{Email, OwnedProject, Project, ProjectID, Resource, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service._
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.{JsBoolean, JsObject, Json}
import play.api.mvc.ControllerComponents

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectController @Inject()(
  usrPrjService: UserProjectService,
  service: ProjectService,
  feedService: ProjectFeedService,
  lookupService: ProjectLookupService,
  trackService: ProjectSupportTrackService,
  dibsOwnSvc: DibsProjectOwnershipService,
  emailOwnSvc: EmailProjectOwnershipService,
  googleOwnSvc: GoogleProjectOwnershipService,
  tumblrOwnSvc: TumblrProjectOwnershipService,
  silhouette: Silhouette[AuthEnv],
  components: ControllerComponents,
  implicit val ec: ExecutionContext
) extends LoveItController(silhouette, components) {

  def getOwnedProjects() = silhouette.SecuredAction.async(implicit req => {
    usrPrjService.get(req.identity.id).map(userProjects => {
      Ok(userProjects)
    })
  })

  def deleteByDibs() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "url").as[Resource]))(implicit req => {
    dibsOwnSvc.delete(req.identity.id, req.body).map(Ok(_))
  })

  def deleteByEmail() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "email").as[Email]))(implicit req => {
    emailOwnSvc.delete(req.identity.id, req.body).map(Ok(_))
  })

  def getProjectsByUser(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    lookupService
      .findByUser(idOrMe(user))
      .map(Ok(_))
  })

  def updateProject(id: ProjectID) = silhouette.SecuredAction(parse.json[Project]).async(implicit req => {
    val user = req.identity.id
    val project = req.body.copy(user = user, _id = id)
    service.update(project).map(Ok(_))
  })

  def getProjectFeed(id: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    val requester = req.identity.id
    lookupService.findById(id).flatMap {
      case Some(project) if project.user == requester =>
        feedService.refresh(project).map(Ok(_))
      case _ =>
        Future.successful(BadRequest)
    }
  })

  def getSupportedByUser(supporter: UserID) = silhouette.SecuredAction.async(implicit req => {
    trackService.
      getSupported(idOrMe(supporter)).
      map(projects => Ok(Json.toJson(projects)))
  })

  def getProject(project: ProjectID) = silhouette.UnsecuredAction.async(implicit req => {
    lookupService.findById(project).map {
      case Some(prj) => Ok(prj)
      case None => NotFound
    }
  })

  def dibsOnUrl() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "url").as[Resource]))(implicit req => {
    dibsOwnSvc.create(req.identity.id, req.body).map(Ok(_))
  })


  def verifyDibs() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "token").as[UUID]))(implicit req => {
    dibsOwnSvc.verify(req.identity.id, req.body).map(Ok(_))
  })

  def reSendDibsVerification() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "url").as[Resource]))(
    implicit req => {
      dibsOwnSvc.sendVerification(req.identity.id, req.body).map(email => Ok(JsBoolean(email.isDefined)))
    }
  )

  def ownershipByEmail() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "email").as[Email]))(implicit req => {
    emailOwnSvc.create(req.identity.id, req.body).map(Ok(_))
  })

  def verifyWithEmail() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "token").as[UUID]))(implicit req => {
    emailOwnSvc.verify(req.identity.id, req.body).map(Ok(_))
  })

  def reSendEmailVerification() = silhouette.SecuredAction.async(parse.json[JsObject].map(json => (json \ "email").as[Email]))(
    implicit req => {
      emailOwnSvc.sendVerification(req.identity.id, req.body).map(email => Ok(JsBoolean(true)))
    }
  )

  def refreshGoogle() = silhouette.SecuredAction.async(implicit req => {
    googleOwnSvc.refresh(req.identity.id).map(Ok(_))
  })

  def refreshTumblr() = silhouette.SecuredAction.async(implicit req => {
    tumblrOwnSvc.refresh(req.identity.id).map(Ok(_))
  })

  def create() = silhouette.SecuredAction.async(parse.json[OwnedProject])(implicit req => {
    service.create(req.identity.id, req.body).map(Ok(_))
  })

  def delete(id: ProjectID) = silhouette.SecuredAction.async(implicit req => {
    service.delete(req.identity.id, id).map(if (_) Ok(JsBoolean(true)) else InternalServerError)
  })

}
