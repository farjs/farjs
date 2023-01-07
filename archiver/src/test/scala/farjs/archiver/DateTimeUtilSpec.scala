package farjs.archiver

import farjs.archiver.DateTimeUtil.parseDateTime
import scommons.nodejs.test.TestSpec

import scala.scalajs.js

class DateTimeUtilSpec extends TestSpec {

  it should "parse date and time when parseDateTime" in {
    //when & then
    parseDateTime("06-28-2019 16:09") shouldBe js.Date.parse("2019-06-28T16:09:00")
    parseDateTime("28.06.19 16:09") shouldBe js.Date.parse("2019-06-28T16:09:00")
    parseDateTime("28.06.99 16:09") shouldBe js.Date.parse("1999-06-28T16:09:00")
    parseDateTime("20190628.161923") shouldBe js.Date.parse("2019-06-28T16:19:23")
  }
}
