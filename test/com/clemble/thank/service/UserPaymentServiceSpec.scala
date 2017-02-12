package com.clemble.thank.service

import com.clemble.thank.model.Payment
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv
import play.api.libs.iteratee.Iteratee

class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val paymentService = application.injector.instanceOf[UserPaymentService]
  val userService = application.injector.instanceOf[UserService]

  "PAYMENT" should {

    "Debit increases User balance" in {
      val user = UserGenerator.generate()

      val savedUser = await(userService.create(user))
      await(paymentService.debit(user, 100))
      val readUser = await(userService.get(user.id).map(_.get))

      savedUser.balance shouldEqual 0
      readUser.balance shouldEqual 100
    }

    "Credit decrease User balance" in {
      val user = UserGenerator.generate()

      val savedUser = await(userService.create(user))
      await(paymentService.debit(user, 100))
      await(paymentService.credit(user, 10))
      val readUser = await(userService.get(user.id).map(_.get))

      savedUser.balance shouldEqual 0
      readUser.balance shouldEqual 90
    }

    "list all transactions" in {
      val user = UserGenerator.generate()

      await(userService.create(user))
      val A = await(paymentService.debit(user, 100))
      val B = await(paymentService.credit(user, 10))
      val payments = await(paymentService.payments(user).run(Iteratee.fold(List.empty[Payment]){ (agg, el) => el :: agg}))

      payments must containAllOf(Seq(A, B))
    }

  }

}