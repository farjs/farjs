package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipEntry._
import scommons.nodejs.test.TestSpec

import scala.scalajs.js.Date.{parse => dt}

class ZipEntrySpec extends TestSpec {

  it should "handle empty zip-archive" in {
    //when
    val results = fromUnzipCommand(
      """Archive:  /test/dir/file.zip
        |Zip file size: 0 bytes, number of entries: 0
        |0 files
        |""".stripMargin)
    
    //then
    results shouldBe Nil
  }

  it should "parse unzip -ZT output" in {
    //when
    val results = fromUnzipCommand(
      """Archive:  /test/dir/file.zip
        |Zip file size: 595630 bytes, number of entries: 18
        |drwxr-xr-x  2.1 unx         0 bx stor 20190628.160903 test/
        |drwxr-xr-x  2.1 unx         0 bx stor 20190628.161923 test/dir/
        |-rw-r--r--  2.1 unx 123456789 bX defN 20190628.161924 test/dir/file.txt
        |18 files, 694287 bytes uncompressed, 591996 bytes compressed:  14.7%
        |""".stripMargin)
    
    //then
    results shouldBe List(
      ZipEntry("", "test", isDir = true, 0, dt("2019-06-28T16:09:03"), "drwxr-xr-x"),
      ZipEntry("test", "dir", isDir = true, 0, dt("2019-06-28T16:19:23"), "drwxr-xr-x"),
      ZipEntry("test/dir", "file.txt", isDir = false, 123456789, dt("2019-06-28T16:19:24"), "-rw-r--r--")
    )
  }
}
