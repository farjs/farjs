package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipEntry._
import scommons.nodejs.test.TestSpec

import scala.scalajs.js

class ZipEntrySpec extends TestSpec {

  it should "parse unzip output" in {
    //when
    val results = fromUnzipCommand(
      """Archive:  /test/dir/file.zip
        |  Length      Date    Time    Name
        |---------  ---------- -----   ----
        |        0  06-28-2019 16:09   test/
        |        0  06-28-2019 16:10   test/dir/
        |123456789  06-28-2019 16:11   test/dir/file.txt
        |---------                     -------
        |   694287                     18 files
        |""".stripMargin)
    
    //then
    results shouldBe List(
      ZipEntry("", "test", isDir = true, datetimeMs = js.Date.parse("2019-06-28T16:09:00")),
      ZipEntry("test", "dir", isDir = true, datetimeMs = js.Date.parse("2019-06-28T16:10:00")),
      ZipEntry("test/dir", "file.txt", size = 123456789, datetimeMs = js.Date.parse("2019-06-28T16:11:00"))
    )
  }
}
