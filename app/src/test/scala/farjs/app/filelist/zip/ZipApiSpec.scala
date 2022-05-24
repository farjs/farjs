package farjs.app.filelist.zip

import farjs.app.filelist.fs.MockChildProcess
import farjs.filelist.api._
import org.scalatest.Succeeded
import scommons.nodejs.ChildProcess.ChildProcessOptions
import scommons.nodejs._
import scommons.nodejs.raw.CreateReadStreamOptions
import scommons.nodejs.stream.Readable
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.util.{StreamReader, SubProcess}

import scala.collection.immutable.ListSet
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.typedarray.Uint8Array

class ZipApiSpec extends AsyncTestSpec {

  private val entriesByParentF = Future.successful(Map(
    "" -> List(
      ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
      ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0, permissions = "drwxr-xr-x")
    ),
    "dir 1" -> List(
      ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0, permissions = "drwxr-xr-x")
    ),
    "dir 1/dir 2" -> List(
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--")
    )
  ))

  //noinspection TypeAnnotation
  class ChildProcess {
    val exec = mockFunction[String, Option[ChildProcessOptions],
      (raw.ChildProcess, Future[(js.Object, js.Object)])]
    val spawn = mockFunction[String, Seq[String], Option[ChildProcessOptions], Future[SubProcess]]

    val childProcess = new MockChildProcess(
      execMock = exec,
      spawnMock = spawn
    )
  }

  it should "return supported capabilities" in {
    //given
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    
    //when & then
    api.capabilities shouldBe Set(
      FileListCapability.read,
      FileListCapability.delete
    )
  }

  it should "return root dir content when readDir(.)" in {
    //given
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    
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
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    
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
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)

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

  it should "read content fully when readFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    val file = path.join(tmpDir, "example.txt")
    val expectedOutput = "hello, World!!!"
    fs.writeFileSync(file, expectedOutput)
    val stdoutStream = fs.createReadStream(file)
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val expectedFilePath = "dir 1/example.txt"
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val stdout = new StreamReader(stdoutStream)
    val subProcess = SubProcess(rawProcess, stdout, Future.unit)
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem("example.txt", size = expectedOutput.length)
    val buff = new Uint8Array(5)

    def loop(source: FileSource, result: StringBuilder): Future[String] = {
      source.readNextBytes(buff).flatMap { bytesRead =>
        if (bytesRead == 0) Future.successful(result.toString())
        else loop(source, result.addAll(buff.subarray(0, bytesRead).map(_.toChar)))
      }
    }

    //when
    val resultF = api.readFile(List(rootPath, "dir 1"), item, 0.0)

    //then
    (for {
      source <- resultF
      output <- loop(source, new StringBuilder)
      _ <- source.close()
    } yield {
      source.file shouldBe expectedFilePath
      output shouldBe expectedOutput
    }).andThen {
      case _ =>
        fs.unlinkSync(file)
        fs.existsSync(file) shouldBe false

        fs.rmdirSync(tmpDir)
        fs.existsSync(tmpDir) shouldBe false
    }
  }

  it should "destroy stream on close if partially read when readFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    val file = path.join(tmpDir, "example.txt")
    val expectedOutput = "hello, World!!!"
    fs.writeFileSync(file, expectedOutput)
    val stdoutStream = fs.createReadStream(file, new CreateReadStreamOptions {
      override val highWaterMark: js.UndefOr[Int] = 5
    })
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val expectedFilePath = "dir 1/example.txt"
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val stdout = new StreamReader(stdoutStream)
    val subProcess = SubProcess(rawProcess, stdout, Future.unit)
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem("example.txt", size = expectedOutput.length)
    val buff = new Uint8Array(5)

    //when
    val resultF = api.readFile(List(rootPath, "dir 1"), item, 0.0)

    //then
    (for {
      source <- resultF
      bytesRead <- source.readNextBytes(buff)
      _ <- source.close()
      maybeContent <- stdout.readNextBytes(5)
    } yield {
      source.file shouldBe expectedFilePath
      bytesRead should be < expectedOutput.length
      maybeContent shouldBe None
    }).andThen {
      case _ =>
        fs.unlinkSync(file)
        fs.existsSync(file) shouldBe false

        fs.rmdirSync(tmpDir)
        fs.existsSync(tmpDir) shouldBe false
    }
  }

  it should "return failed Future on readNextBytes if error when readFile" in {
    //given
    val stdoutStream = Readable.from(new js.Array[String](0))
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val expectedFilePath = "dir 1/example.txt"
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val stdout = new StreamReader(stdoutStream)
    val subProcess = SubProcess(rawProcess, stdout, Future.failed(new Exception("test error")))
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem("example.txt", size = 5)
    val buff = new Uint8Array(5)

    //when
    val resultF = api.readFile(List(rootPath, "dir 1"), item, 0.0)

    //then
    for {
      source <- resultF
      error <- source.readNextBytes(buff).failed
      _ <- source.close()
    } yield {
      source.file shouldBe expectedFilePath
      error.getMessage shouldBe "test error"
    }
  }

  it should "spawn ChildProcess when extract" in {
    //given
    val expectedOutput = "hello, World!!!"
    val stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, Future.unit)
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    val filePath = s"$rootPath/dir 1/file 2.txt"

    //then
    childProcess.spawn.expects("unzip", List("-p", zipPath, filePath), *).onCall { (_, _, options) =>
      inside(options) { case Some(opts) =>
        opts.windowsHide shouldBe true
        childProcess.childProcess
      }
      Future.successful(subProcess)
    }

    //when
    val resultF = api.extract(zipPath, filePath)

    def loop(reader: StreamReader, result: String): Future[String] = {
      reader.readNextBytes(5).flatMap {
        case None => Future.successful(result)
        case Some(content) =>
          loop(reader, result + content.toString)
      }
    }

    //then
    resultF.flatMap(res => loop(res.stdout, "")).map { output =>
      output shouldBe expectedOutput
    }
  }

  it should "execute zip command recursively when delete" in {
    //given
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val result = (new js.Object, new js.Object)
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, Future.successful(Map(
      "" -> List(
        ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0, permissions = "drwxr-xr-x")
      ),
      "dir 1" -> List(
        ZipEntry("dir 1", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
        ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0, permissions = "drwxr-xr-x")
      ),
      "dir 1/dir 2" -> List(
        ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--"),
        ZipEntry("dir 1/dir 2", "dir 3", isDir = true, datetimeMs = 7.0, permissions = "drwxr-xr-x")
      )
    )))
    val parent = s"$rootPath/dir 1"
    val items = List(
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true)
    )

    //then
    val commands = ArrayBuffer.empty[String]
    childProcess.exec.expects(*, *).twice().onCall { (command, options) =>
      commands += command
      assertObject(options.get, new ChildProcessOptions {
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = for {
      _ <- api.delete(parent, items)
      res <- api.readDir(parent)
    } yield res

    //then
    resultF.map { res =>
      res.items should be (empty)
      commands shouldBe List(
        s"""zip -qd "$zipPath" "dir 1/dir 2/file 2" "dir 1/dir 2/dir 3/"""",
        s"""zip -qd "$zipPath" "dir 1/file 1" "dir 1/dir 2/""""
      )
    }
  }

  it should "spawn zip command when addToZip" in {
    //given
    val stdout = new StreamReader(Readable.from(Buffer.from("")))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, Future.unit)
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val parent = "test dir"
    val zipFile = "test.zip"
    val items = ListSet("item 1", "item 2")

    //then
    childProcess.spawn.expects(
      "zip",
      List("-qr", "test.zip", "item 1", "item 2"),
      *
    ).onCall { (_, _, options) =>
      inside(options) { case Some(opts) =>
        opts.cwd shouldBe parent
        opts.windowsHide shouldBe true
      }
      Future.successful(subProcess)
    }

    //when
    val resultF = ZipApi.addToZip(zipFile, parent, items)

    //then
    resultF.map(_ => Succeeded)
  }

  it should "spawn unzip and parse output when readZip" in {
    //given
    val expectedOutput =
      """Archive:  /test/dir/file.zip
        |Zip file size: 595630 bytes, number of entries: 18
        |-rw-r--r--  2.1 unx     1 bX defN 20190628.161923 test/dir/file.txt
        |18 files
        |""".stripMargin
    val stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, Future.unit)
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val zipPath = "/dir/filePath.zip"

    //then
    childProcess.spawn.expects("unzip", List("-ZT", zipPath), *).onCall { (_, _, options) =>
      inside(options) { case Some(opts) =>
        opts.windowsHide shouldBe true
      }
      Future.successful(subProcess)
    }

    //when
    val resultF = ZipApi.readZip(zipPath)

    //then
    resultF.map { res =>
      res shouldBe Map(
        "test/dir" -> List(
          ZipEntry("test/dir", "file.txt", isDir = false, 1, js.Date.parse("2019-06-28T16:19:23"), "-rw-r--r--")
        ),
        "test" -> List(
          ZipEntry("test", "dir", isDir = true, 0, js.Date.parse("2019-06-28T16:19:23"), "drw-r--r--")
        ),
        "" -> List(
          ZipEntry("", "test", isDir = true, 0, js.Date.parse("2019-06-28T16:19:23"), "drw-r--r--")
        )
      )
    }
  }
  
  it should "infer dirs when groupByParent" in {
    //given
    val entriesF = List(
      ZipEntry("dir 1/dir 2/dir 3", "file 3", size = 7.0, datetimeMs = 8.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1/dir 2/dir 3", "file 4", size = 9.0, datetimeMs = 10.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--"),
      ZipEntry("dir 1", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--")
    )
    
    //when
    val result = ZipApi.groupByParent(entriesF)
    
    //then
    result shouldBe Map(
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
