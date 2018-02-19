package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.EOMChargeService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class EOMChargeController @Inject()(
                                     service: EOMChargeService,
                                     silhouette: Silhouette[AuthEnv],
                                     components: ControllerComponents,
                                     implicit val ec: ExecutionContext
                                   ) extends LoveItController(components) {

  def getMyCSV() = silhouette.SecuredAction.async(req => {
    val fCSV = for {
      charges <- service.findByUser(req.identity.id)
    } yield {
      val csv = charges.
        flatMap(charge => charge.
          transactions.
          groupBy(_.resource).
          map({
            case (resource, transactions) =>
              List(charge.yom.toString, transactions.size, resource.stringify()).mkString(",")
          })
        )

      csv.mkString("\n")
    }

    fCSV.map(Ok(_))
  })


  def listMy() = silhouette.SecuredAction.async(req => {
    service.findByUser(req.identity.id).map(Ok(_))
  })

}