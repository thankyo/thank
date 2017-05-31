package com.clemble.loveit.user.model

import com.clemble.loveit.common.model.{HttpResource, Resource}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceSpec extends Specification {

  "generate parent" in {
    val parts = Resource.from("http/example.com/some/what").parent()
    parts must beEqualTo(HttpResource("example.com/some/what"))
  }

  "generate all parent URL's" in {
    val parts = Resource.from("http/example.com/some/what").parents()
    parts must beEqualTo(List(
      HttpResource("example.com/some/what"),
      HttpResource("example.com/some"),
      HttpResource("example.com")
    ))
  }

  "normalize" in {
    val resource = HttpResource("example.com/some/what")
    for {
      variation <- ResourceSpec.generateVariations(resource.uri)
    } yield {
      Resource.from(variation) shouldEqual resource
    }
  }

}

object ResourceSpec {

  def generateVariations(masterURI: String): List[String] = {
    List(
      masterURI,
      s"http//$masterURI",
      s"https//$masterURI",
      s"http/////$masterURI",
      s"https/////$masterURI",
      s"http/$masterURI",
      s"https/$masterURI",
      s"http://$masterURI",
      s"https://$masterURI",
      s"http://///$masterURI",
      s"https://///$masterURI",
      s"http:/$masterURI",
      s"https:/$masterURI"
    )
  }

}