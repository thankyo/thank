package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.payment.service.repository.ThankTransactionRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.{Source}
import com.clemble.loveit.common.model.UserID
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.play.json._

@Singleton
case class MongoThankTransactionRepository @Inject()(
                                                      @Named("userPayment") collection: JSONCollection,
                                                      implicit val m: Materializer,
                                                      implicit val ec: ExecutionContext)
  extends ThankTransactionRepository {

  override def save(transaction: ThankTransaction): Future[Boolean] = {
    val selector = Json.obj("_id" -> transaction.user, "pending.resource" -> Json.obj("$ne" -> transaction.resource))
    val update = Json.obj("$push" -> Json.obj("pending" -> transaction))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def findByUser(user: UserID): Source[ThankTransaction, _] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("pending" -> 1)
    val fSource = collection.find(selector, projection).
      one[JsObject].
      map(pendingOpt => {
        pendingOpt.
          flatMap(obj => (obj \ "pending").asOpt[Seq[ThankTransaction]]).
          map(p => Source.fromIterator(() => p.iterator)).
          getOrElse(Source.empty[ThankTransaction])
      })
    Source.fromFuture(fSource).flatMapConcat(s => s)
  }

  override def removeAll(thanks: Seq[ThankTransaction]): Future[Boolean] = {
    val selector = Json.obj("_id" -> Json.obj("$in" -> thanks.map(_.user).distinct))
    val update = Json.obj("$pull" -> Json.obj("pending" -> Json.obj("$in" -> thanks)))
    MongoSafeUtils.safe(collection.update(selector, update, multi = true).map(_.ok))
  }

}
