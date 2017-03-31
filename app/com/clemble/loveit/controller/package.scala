package com.clemble.loveit

import akka.util.ByteString
import com.clemble.loveit.model._
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.payment.model.PaymentTransaction
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.Format

package object controller {

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

  implicit val thankExceptionWriteable = jsonToWriteable[ThankException]
  implicit val paymentTransactionWriteable = jsonToWriteable[PaymentTransaction]
  implicit val thankTransactionWriteable = jsonToWriteable[ThankTransaction]
  implicit val resourceOwnershipWriteable = jsonToWriteable[ResourceOwnership]
  implicit val userWriteable = jsonToWriteable[User]
  implicit val thankWriteable = jsonToWriteable[Thank]

}