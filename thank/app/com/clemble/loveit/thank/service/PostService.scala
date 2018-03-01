package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, Project}
import com.clemble.loveit.thank.service.repository.PostRepository

import scala.concurrent.{ExecutionContext, Future}

trait PostService {

  def getPostOrProject(uri: Resource): Future[Either[Post, Project]]

  def create(og: OpenGraphObject): Future[Post]

  def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean]

  def updateProject(owner: Project): Future[Boolean]

  def findByTags(tags: Set[Tag]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def findByProject(project: ProjectID): Future[List[Post]]

  def hasSupported(giver: UserID, uri: Resource): Future[Boolean]

  def thank(supporter: UserID, url: Resource): Future[Post]

}

@Singleton
case class SimplePostService @Inject()(
                                        thankEventBus: ThankEventBus,
                                        prjService: ProjectService,
                                        postRefreshService: PostEnrichService,
                                        postRepo: PostRepository,
                                        implicit val ec: ExecutionContext
                                      ) extends PostService {

  override def hasSupported(supporter: UserID, res: Resource): Future[Boolean] = {
    getPostOrProject(res) map {
      case Left(post) => post.thank.isSupportedBy(supporter)
      case Right(_) => false
    }
  }

  override def getPostOrProject(res: Resource): Future[Either[Post, Project]] = {
    def findProjectIfNoPost(postOpt: Option[Post]): Future[Either[Post, Project]] = {
      postOpt match {
        case Some(post) =>
          Future.successful(Left(post))
        case None =>
          prjService
            .findProject(res)
            .map(_.map(Right(_)).getOrElse({ throw ResourceException.ownerMissing() }))
      }
    }

    postRepo.findByResource(res).flatMap(findProjectIfNoPost)
  }


  override def findByTags(tags: Set[Tag]): Future[List[Post]] = {
    postRepo.findByTags(tags)
  }

  override def findByAuthor(author: UserID): Future[List[Post]] = {
    postRepo.findByAuthor(author)
  }

  override def findByProject(project: ProjectID): Future[List[Post]] = {
    postRepo.findByProject(project)
  }

  override def create(og: OpenGraphObject): Future[Post] = {
    val res = og.url
    getPostOrProject(res).flatMap(_ match {
      case Left(post) =>
        val updPost = post.withOg(og)
        postRepo.update(updPost).filter(_ == true).map(_ => updPost)
      case Right(project) =>
        val post = Post.from(og, project)
        postRepo.save(post).filter(_ == true).map((_) => post)
    })
  }

  override def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean] = {
    postRepo.assignTags(uri, tags)
  }

  override def updateProject(project: Project): Future[Boolean] = {
    postRepo.updateProject(project)
  }

  override def thank(giver: UserID, res: Resource): Future[Post] = {
    getPostOrProject(res) flatMap {
      case Left(post) =>
        Future.successful(post)
      case Right(_) =>
        postRefreshService
          .enrich(OpenGraphObject(url = res))
          .flatMap(create)
    } flatMap (post => {
      postRepo.markSupported(giver, res).map(increased => {
        if (increased) {
          thankEventBus.publish(ThankEvent(giver, post.project, res))
          post.copy(thank = post.thank.withSupporter(giver))
        } else {
          post
        }
      })
    })
  }

}