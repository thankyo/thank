package com.clemble.thank.service.repository

import akka.stream.scaladsl.Source
import com.clemble.thank.model.{UserAware, UserID}

trait UserAwareRepository[T <: UserAware] {

  /**
    * Find all entities related to the user
    *
    * @param user user identifier
    * @return all payments done by the user
    */
  def findByUser(user: UserID): Source[T, _]

}
