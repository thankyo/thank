package com.clemble.thank.service

import com.clemble.thank.model.{Thank, UserId}

import scala.concurrent.Future

trait ThankService {

  def findAll(uri: String): Future[Thank]

  def thank(user: UserId, uri: String): Future[Thank]

}
