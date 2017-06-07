package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency
import play.api.libs.json._

/**
  * User view for Payments
  */
trait UserPayment {
  /**
    * User identifier
    */
  val id: UserID
  /**
    * Current balance which can be negative
    */
  val balance: Amount
  /**
    * BankDetails to use for withdraw and
    */
  val bankDetails: Option[BankDetails]
  /**
    * Monthly transaction limit
    */
  val monthlyLimit: Money
  /**
    * Pending transactions
    */
  val pending: List[ThankTransaction]
}

object UserPayment {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

  implicit val jsonFormat: Reads[UserPayment] = new Reads[UserPayment] {
    override def reads(json: JsValue): JsResult[UserPayment] = SimpleUserPayment.jsonFormat.reads(json)
  }
}

private case class SimpleUserPayment(
                            id: UserID,
                            balance: Amount,
                            bankDetails: Option[BankDetails],
                            monthlyLimit: Money,
                            pending: List[ThankTransaction]
) extends UserPayment

private object SimpleUserPayment {

  implicit val jsonFormat = Json.format[SimpleUserPayment]

}