package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Resource}
import com.clemble.loveit.thank.model.{OpenGraphObject, Post}

trait PostTestService extends ServiceSpec {

  def someChildResource(res: Resource): Resource = {
    Resource.from(s"http://${res.stringify()}/${someRandom[Int]}/${someRandom[Int]}")
  }

  def createPost(resource: Resource = someRandom[Resource]): Resource = {
    // Step 1. Create owner & resource to own
    val owner = createUser()
    val resource = someRandom[Resource]
    // Step 2. Assign ownership to resource
    assignOwnership(owner, resource)
    // Step 3. Create OGO with specific resource
    val ogo = someRandom[OpenGraphObject].copy(url = resource.stringify())
    createPost(ogo)
    // Step 4. Returning original resource
    resource
  }

  def createPost(ogo: OpenGraphObject): Post

}

class RepoPostTestService extends ServiceSpec {

}
