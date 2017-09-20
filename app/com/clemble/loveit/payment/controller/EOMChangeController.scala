package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{InjectedController}

import scala.concurrent.ExecutionContext

@Singleton
class EOMChangeController @Inject()(
                                     chargeRepo: EOMChargeRepository,
                                     silhouette: Silhouette[AuthEnv],
                                     implicit val ec: ExecutionContext
                                   ) extends InjectedController {

  def listMy() = silhouette.SecuredAction(req => {
    val charges = chargeRepo.findByUser(req.identity.id)
    Ok.chunked(charges)
  })

}