package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipEntry._
import scommons.nodejs.test.TestSpec

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
      ZipEntry("", "test", isDir = true, datetimeMs = 1561730940000.0),
      ZipEntry("test", "dir", isDir = true, datetimeMs = 1561731000000.0),
      ZipEntry("test/dir", "file.txt", size = 123456789, datetimeMs = 1561731060000.0)
    )
  }

  it should "parse date and time when parseDateTime" in {
    //when & then
    parseDateTime("06-28-2019 16:09") shouldBe 1561730940000.0
  }
}
