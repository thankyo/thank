package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.CommonSocialProfileGenerator
import com.clemble.loveit.thank.model.ResourceOwnership
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  lazy val service = dependency[ResourceOwnershipService]

  def listResources(userAuth: Seq[(String, String)]): Set[ResourceOwnership] = {
    await(service.list(userAuth.head._2))
  }

  def assignOwnership(userAuth: Seq[(String, String)], resource: ResourceOwnership): ResourceOwnership = {
    await(service.assign(userAuth.head._2, resource))
  }

  "POST" should {

    "assign partial ownership" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resource = ResourceOwnership.partial(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(userAuth, resource)

      val expectedResources = List(
        ResourceOwnership.full(Resource from social.loginInfo),
        resource
      )
      eventually(listResources(userAuth) shouldEqual expectedResources)
    }

    "assign full ownership" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resource = ResourceOwnership.full(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(userAuth, resource)

      val expectedResources = List(
        ResourceOwnership.full(Resource from social.loginInfo),
        resource
      )
      eventually(listResources(userAuth) shouldEqual expectedResources)
    }

    "prohibit assigning same resource" in {
      val A = createUser()
      val B = createUser()

      val resource = ResourceOwnership.full(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, resource) must throwA[ThankException]
    }

    "prohibit assigning sub resource in case full ownership" in {
      val A = createUser()
      val B = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = ResourceOwnership.full(Resource from uri)
      val subResource = ResourceOwnership.full(Resource from s"$uri/${RandomStringUtils.random(10)}")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, subResource) must throwA[ThankException]
    }

    "prohibit assigning sub resource in case of partial ownership" in {
      val A = createUser()
      val B = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = ResourceOwnership.partial(Resource from uri)
      val subResource = ResourceOwnership.full(Resource from s"$uri/${RandomStringUtils.random(10)}")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, subResource) mustEqual subResource
    }

  }

}
