package farjs.app.filelist.zip

import farjs.app.filelist.fs.MockChildProcess
import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.nodejs.ChildProcess.ChildProcessOptions
import scommons.nodejs.raw
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js

class ZipApiSpec extends AsyncTestSpec {

  private val entriesF = Future.successful(List(
    ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0, permissions = "drwxr-xr-x"),
    ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
    ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0, permissions = "drwxr-xr-x"),
    ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--")
  ))

  //noinspection TypeAnnotation
  class ChildProcess {
    val exec = mockFunction[String, Option[ChildProcessOptions],
      (raw.ChildProcess, Future[(js.Object, js.Object)])]

    val childProcess = new MockChildProcess(
      execMock = exec
    )
  }

  it should "return root dir content when readDir(.)" in {
    //given
    val filePath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(filePath, rootPath, entriesF)
    
    //when
    val resultF = api.readDir(None, FileListItem.currDir.name)

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe rootPath
      isRoot shouldBe false
      items shouldBe List(
        FileListItem("file 1", size = 2.0, mtimeMs = 3.0, permissions = "-rw-r--r--"),
        FileListItem("dir 1", isDir = true, mtimeMs = 1.0, permissions = "drwxr-xr-x")
      )
    })
  }

  it should "return root dir content when readDir(..)" in {
    //given
    val filePath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(filePath, rootPath, entriesF)
    
    //when
    val resultF = api.readDir(Some(s"$rootPath/dir 1/dir 2"), FileListItem.up.name)

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"$rootPath/dir 1"
      isRoot shouldBe false
      items shouldBe List(
        FileListItem("dir 2", isDir = true, mtimeMs = 4.0, permissions = "drwxr-xr-x")
      )
    })
  }

  it should "return sub-dir content when readDir" in {
    //given
    val filePath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(filePath, rootPath, entriesF)
    
    //when
    val resultF = api.readDir(Some(s"$rootPath/dir 1"), "dir 2")

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"$rootPath/dir 1/dir 2"
      isRoot shouldBe false
      items shouldBe List(
        FileListItem("file 2", size = 5.0, mtimeMs = 6.0, permissions = "-rw-r--r--")
      )
    })
  }

  it should "call unzip and parse output when readZip" in {
    //given
    val childProcess = new ChildProcess
    val filePath = "/dir/filePath.zip"
    val output =
      """Archive:  /test/dir/file.zip
        |Zip file size: 595630 bytes, number of entries: 18
        |-rw-r--r--  2.1 unx     1 bX defN 20190628.161923 test/dir/file.txt
        |18 files
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe s"""unzip -ZT "$filePath""""
      assertObject(options.get, new ChildProcessOptions {
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = ZipApi.readZip(childProcess.childProcess, filePath)

    //then
    resultF.map { res =>
      res shouldBe List(
        ZipEntry("test/dir", "file.txt", isDir = false, 1, js.Date.parse("2019-06-28T16:19:23"), "-rw-r--r--")
      )
    }
  }
  
  it should "infer dirs when groupByParent" in {
    //given
    val entriesF = Future.successful(List(
      ZipEntry("dir 1/dir 2/dir 3", "file 3", size = 7.0, datetimeMs = 8.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1/dir 2/dir 3", "file 4", size = 9.0, datetimeMs = 10.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--")
    ))
    
    //when
    val resultF = ZipApi.groupByParent(entriesF)
    
    //then
    resultF.map { res =>
      res shouldBe Map(
        "dir 1/dir 2/dir 3" -> List(
          ZipEntry("dir 1/dir 2/dir 3", "file 4", size = 9.0, datetimeMs = 10.0, permissions = "-rw-r--r--"),
          ZipEntry("dir 1/dir 2/dir 3", "file 3", size = 7.0, datetimeMs = 8.0, permissions = "-rw-r--r--")
        ),
        "dir 1/dir 2" -> List(
          ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--"),
          ZipEntry("dir 1/dir 2", "dir 3", isDir = true, datetimeMs = 8.0, permissions = "drw-r--r--")
        ),
        "dir 1" -> List(
          ZipEntry("dir 1", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
          ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 8.0, permissions = "drw-r--r--")
        ),
        "" -> List(
          ZipEntry("", "dir 1", isDir = true, datetimeMs = 8.0, permissions = "drw-r--r--")
        )
      )
    }
  }
}
