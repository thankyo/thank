package com.clemble.thank.service.repository

import akka.stream.scaladsl.Sink
import com.clemble.thank.model.Payment
import com.clemble.thank.test.util.{PaymentGenerator, UserGenerator}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.collection.immutable.Seq

@RunWith(classOf[JUnitRunner])
class PaymentRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repository = application.injector.instanceOf[PaymentRepository]

  "CREATE" should {

    "save all payments for the user" in {
      val user = UserGenerator.generate()
      val A = PaymentGenerator.generate(user)
      val B = PaymentGenerator.generate(user)

      val fTransactions = for {
        _ <- repository.save(A)
        _ <- repository.save(B)
        transactions <- repository.findByUser(user.id).runWith(Sink.seq[Payment])
      } yield {
        transactions
      }
      val transactions = await(fTransactions)

      transactions must containAllOf(Seq(A, B)).exactly
    }

  }

}
