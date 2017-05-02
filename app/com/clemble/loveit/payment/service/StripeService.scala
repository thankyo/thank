package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{PaymentRequest, PaymentTransaction}

import scala.concurrent.Future

/**
  * Stipe processing service
  */
trait StripeService extends PaymentService {

  def process(user: UserID, req: PaymentRequest): Future[PaymentTransaction]

}
