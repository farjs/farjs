package scommons.nodejs

import scommons.nodejs.test.TestSpec

class URLSpec extends TestSpec {

  it should "create new URL" in {
    //when & then
    new URL("https://example.org/").toString shouldBe "https://example.org/"
    new URL("/foo", "https://example.org/").toString shouldBe "https://example.org/foo"
    new URL("/foo", new URL("https://example.org/")).toString shouldBe "https://example.org/foo"
  }
}
