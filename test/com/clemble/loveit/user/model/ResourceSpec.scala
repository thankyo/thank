package com.clemble.loveit.user.model

import com.clemble.loveit.common.model._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceSpec extends Specification {

  "generate parent" in {
    val parts = "http://example.com/some/what".parent()
    parts shouldEqual Some("http://example.com/some")
  }

  "generate all parent URL's" in {
    val parts = "http://example.com/some/what".parents()
    parts must beEqualTo(List(
      "http://example.com/some/what",
      "http://example.com/some",
      "http://example.com"
    ))
  }

  "normalize" in {
    "example.com".normalize() shouldEqual "https://example.com"
    "https://example.com/".normalize() shouldEqual "https://example.com"
  }

  "domain" in {
    "https://www.some.com".toParentDomain() shouldEqual "some.com"
    "https://some.com".toParentDomain() shouldEqual "some.com"
    "http://www.some.com".toParentDomain() shouldEqual "some.com"
    "http://some.com".toParentDomain() shouldEqual "some.com"
  }

}