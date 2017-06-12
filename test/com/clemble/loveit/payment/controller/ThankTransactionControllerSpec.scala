package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.payment.model.ThankTransaction
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class ThankTransactionControllerSpec extends ControllerSpec {

  "GET" should {

    "List on new user" in {
      val user = createUser()
      val req = sign(user, FakeRequest(GET, s"/api/v1/payment/pending/my"))
      val fRes = route(application, req).get

      val res = await(fRes)
      val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[ThankTransaction])
      val payments = respSource.toSeq
      payments shouldEqual Nil
    }

  }

}
