package com.clemble.thank.controller

import com.clemble.thank.model.User
import com.clemble.thank.test.util.{CommonSocialProfileGenerator}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  "CREATE" should {

    "Support single create" in {
      val socialProfile = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(socialProfile)

      val savedUser = getMyUser()(userAuth)
      val expectedUser = (User from socialProfile).copy(id = savedUser.id)
      savedUser must beEqualTo(expectedUser)
    }

    "Return same user on the same authentication" in {
      val socialProfile = CommonSocialProfileGenerator.generate()
      val firstAuth = createUser(socialProfile)
      val firstUser = getMyUser()(firstAuth)

      val secondAuth = createUser(socialProfile)
      val secondUser = getMyUser()(firstAuth)

      firstAuth shouldNotEqual secondAuth
      secondUser shouldEqual firstUser
    }

  }

}
