package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{ChargeAccount, StripeChargeAccount, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository.{ChargeAccountRepository}
import com.google.common.collect.{ImmutableMap}
import com.stripe.Stripe
import com.stripe.model.Card

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
  * [[ChargeAccount]] integration service
  */
trait ChargeAccountService {

  def getChargeAccount(user: UserID): Future[Option[ChargeAccount]]

  def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount]

}

@Singleton
case class SimpleChargeAccountService @Inject()(repo: ChargeAccountRepository, chAccService: ChargeAccountConverter, implicit val ec: ExecutionContext) extends ChargeAccountService {


  override def getChargeAccount(user: UserID): Future[Option[ChargeAccount]] = {
    repo.getChargeAccount(user)
  }

  override def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount] = {
    for {
      chAcc <- chAccService.process(token)
      updated <- repo.setChargeAccount(user, chAcc)
    } yield {
      if (!updated) throw PaymentException.failedToLinkChargeAccount(user)
      chAcc
    }
  }

}


/**
  * Payment processing service abstraction
  */
sealed trait ChargeAccountConverter {

  /**
    * Process payment request by the user
    */
  def process(token: String): Future[_ <: ChargeAccount]

}


/**
  * Stripe processing service
  */
@Singleton
class StripeChargeAccountConverter(apiKey: String) extends ChargeAccountConverter {
  Stripe.apiKey = apiKey

  import com.stripe.model.Customer

  private def toInternalChargeAccount(customer: Customer): StripeChargeAccount = {
    val (brand, last4) = customer.
      getSources.
      getData().
      to[List].
      collectFirst({ case card: Card => (Option(card.getBrand), Option(card.getLast4))}).
      getOrElse(None -> None)
    StripeChargeAccount(customer.getId, brand, last4)
  }

  override def process(token: String): Future[StripeChargeAccount] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val chargeAccount = toInternalChargeAccount(customer)
    // Step 4. Generating successful request
    Future.successful(chargeAccount)
  }

}