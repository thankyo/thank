package com.clemble.loveit.payment.model

import java.time.YearMonth

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import org.joda.time.DateTime
import play.api.libs.json._

case object PayoutStatus extends Enumeration {
  type PayoutStatus = Value
  val Pending, Running, Success, Failed, NoBankAccount = Value

  implicit val jsonFormat = new Format[PayoutStatus] {
    override def writes(o: PayoutStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[PayoutStatus] = json match {
      case JsString(str) => JsSuccess(PayoutStatus.withName(str))
    }
  }
}

case class EOMPayout(
                      user: UserID,
                      yom: YearMonth,
                      destination: Option[PayoutAccount],
                      amount: Money,
                      status: PayoutStatus,
                      created: DateTime = new DateTime()
) extends Transaction with EOMAware

object EOMPayout {

  implicit val jsonFormat = Json.format[EOMPayout]

  implicit val writeableJson = WriteableUtils.jsonToWriteable[EOMPayout]

}