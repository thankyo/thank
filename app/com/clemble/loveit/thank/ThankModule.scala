package com.clemble.loveit.thank

import com.clemble.loveit.thank.service.repository.{OwnershipVerificationRepository, ThankRepository}
import com.clemble.loveit.thank.service.repository.mongo.{MongoOwnershipVerificationRepository, MongoThankRepository}
import com.clemble.loveit.thank.service._
import com.google.inject.Provides
import javax.inject.{Named, Singleton}

import com.clemble.loveit.common.model.Resource
import net.codingwell.scalaguice.ScalaModule
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class ThankModule extends ScalaModule {

  override def configure(): Unit = {
    bind(classOf[ThankService]).to(classOf[SimpleThankService])
    bind(classOf[ThankRepository]).to(classOf[MongoThankRepository])

    bind(classOf[ResourceOwnershipService]).to(classOf[SimpleResourceOwnershipService])

    ownershipVerification()
  }

  def ownershipVerification(): Unit = {
    bind(classOf[OwnershipVerificationGenerator]).to(classOf[CryptOwnershipVerificationGenerator])
    bind(classOf[OwnershipVerificationRepository]).to(classOf[MongoOwnershipVerificationRepository])
    bind(classOf[MetaTagReader]).to(classOf[WSMetaTagReader])
    bind(classOf[OwnershipVerificationService]).to(classOf[SimpleOwnershipVerificationService])
  }

  @Provides
  @Singleton
  def resourceVerificationService(httpVerification: HttpOwnershipVerificationService): ResourceVerificationService[Resource] = {
    ResourceVerificationFacade(httpVerification)
  }

  @Provides
  @Singleton
  @Named("thank")
  def thankMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("thank", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

}
