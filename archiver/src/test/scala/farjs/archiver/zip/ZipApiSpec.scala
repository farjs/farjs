package farjs.archiver.zip

import farjs.filelist.MockChildProcess
import farjs.filelist.api.FileListItemSpec.assertFileListItems
import farjs.filelist.api._
import farjs.filelist.util.ChildProcess.ChildProcessOptions
import farjs.filelist.util.{StreamReader, SubProcess}
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.raw.CreateReadStreamOptions
import scommons.nodejs.stream.Readable
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.typedarray.Uint8Array

class ZipApiSpec extends AsyncTestSpec {

  private val entriesByParentF = Future.successful(new js.Map[String, js.Array[FileListItem]](js.Array(
    "" -> js.Array[FileListItem](
      ZipEntry("", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
      ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0, permissions = "drwxr-xr-x")
    ),
    "dir 1" -> js.Array[FileListItem](
      ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0, permissions = "drwxr-xr-x")
    ),
    "dir 1/dir 2" -> js.Array[FileListItem](
      ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--")
    )
  )))

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
    api.capabilities.toSet shouldBe Set(
      FileListCapability.read,
      FileListCapability.delete
    )
  }

  it should "return root dir content when readDir('', .)" in {
    //given
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    
    //when
    val resultF = api.readDir("", FileListItem.currDir.name).toFuture

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe rootPath
      isRoot shouldBe false
      assertFileListItems(items.toList, List(
        ZipEntry("", "file 1", isDir = false, size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
        ZipEntry("", "dir 1", isDir = true, size = 0.0, datetimeMs = 1.0, permissions = "drwxr-xr-x")
      ))
    })
  }

  it should "return root dir content when readDir(..)" in {
    //given
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)
    
    //when
    val resultF = api.readDir(s"$rootPath/dir 1/dir 2", FileListItem.up.name).toFuture

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"$rootPath/dir 1"
      isRoot shouldBe false
      assertFileListItems(items.toList, List(
        ZipEntry("", "dir 2", isDir = true, size = 0.0, datetimeMs = 4.0, permissions = "drwxr-xr-x")
      ))
    })
  }

  it should "return sub-dir content when readDir" in {
    //given
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, entriesByParentF)

    //when
    val resultF = api.readDir(s"$rootPath/dir 1", "dir 2").toFuture

    //then
    resultF.map(inside(_) { case FileListDir(path, isRoot, items) =>
      path shouldBe s"$rootPath/dir 1/dir 2"
      isRoot shouldBe false
      assertFileListItems(items.toList, List(
        ZipEntry("", "file 2", isDir = false, size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--")
      ))
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
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem.copy(FileListItem("example.txt"))(size = expectedOutput.length)
    val buff = new Uint8Array(5)

    def loop(source: FileSource, result: StringBuilder): Future[String] = {
      source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
        if (bytesRead == 0) Future.successful(result.toString())
        else loop(source, result.addAll(buff.subarray(0, bytesRead).map(_.toChar)))
      }
    }

    //when
    val resultF = api.readFile(s"$rootPath/dir 1", item, 0.0).toFuture

    //then
    (for {
      source <- resultF
      output <- loop(source, new StringBuilder)
      _ <- source.close().toFuture
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
    val expectedOutput = "hello"
    fs.writeFileSync(file, expectedOutput)
    val stdoutStream = fs.createReadStream(file, new CreateReadStreamOptions {
      override val highWaterMark: js.UndefOr[Int] = 5
    })
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val expectedFilePath = "dir 1/example.txt"
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val stdout = new StreamReader(stdoutStream)
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem.copy(FileListItem("example.txt"))(size = expectedOutput.length * 2)
    val buff = new Uint8Array(5)

    //when
    val resultF = api.readFile(s"$rootPath/dir 1", item, 0.0).toFuture

    //then
    (for {
      source <- resultF
      _ <- source.readNextBytes(buff).toFuture.map { bytesRead =>
        bytesRead shouldBe expectedOutput.length
      }
      _ <- source.readNextBytes(buff).toFuture.map { bytesRead =>
        bytesRead shouldBe 0
      }
      _ <- source.close().toFuture
      maybeContent <- stdout.readNextBytes(5).toFuture
    } yield {
      source.file shouldBe expectedFilePath
      maybeContent shouldBe js.undefined
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
    val error = js.Error("test error")
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[js.UndefOr[js.Error]](error))
    val api = new ZipApi(zipPath, rootPath, entriesByParentF) {
      override def extract(zipPath: String, filePath: String): Future[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        Future.successful(subProcess)
      }
    }
    val item = FileListItem.copy(FileListItem("example.txt"))(size = 5)
    val buff = new Uint8Array(5)

    //when
    val resultF = api.readFile(s"$rootPath/dir 1", item, 0.0).toFuture

    //then
    for {
      source <- resultF
      error <- source.readNextBytes(buff).toFuture.failed
      _ <- source.close().toFuture
    } yield {
      source.file shouldBe expectedFilePath
      error.getMessage shouldBe "Error: test error"
    }
  }

  it should "spawn ChildProcess when extract" in {
    //given
    val expectedOutput = "hello, World!!!"
    val stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
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
      reader.readNextBytes(5).toFuture.map(_.toOption).flatMap {
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

  it should "spawn zip command recursively when delete" in {
    //given
    val stdout = new StreamReader(Readable.from(Buffer.from(
      """  deleting 1
        |  deleting 2
        |""".stripMargin)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val zipPath = "/dir/filePath.zip"
    val rootPath = "zip://filePath.zip"
    val api = new ZipApi(zipPath, rootPath, Future.successful(new js.Map[String, js.Array[FileListItem]](js.Array(
      "" -> js.Array[FileListItem](
        ZipEntry("", "dir 1", isDir = true, datetimeMs = 1.0, permissions = "drwxr-xr-x")
      ),
      "dir 1" -> js.Array[FileListItem](
        ZipEntry("dir 1", "file 1", size = 2.0, datetimeMs = 3.0, permissions = "-rw-r--r--"),
        ZipEntry("dir 1", "dir 2", isDir = true, datetimeMs = 4.0, permissions = "drwxr-xr-x")
      ),
      "dir 1/dir 2" -> js.Array[FileListItem](
        ZipEntry("dir 1/dir 2", "file 2", size = 5.0, datetimeMs = 6.0, permissions = "-rw-r--r--"),
        ZipEntry("dir 1/dir 2", "dir 3", isDir = true, datetimeMs = 7.0, permissions = "drwxr-xr-x")
      )
    ))))
    val parent = s"$rootPath/dir 1"
    val items = js.Array(
      FileListItem("file 1"),
      FileListItem("dir 2", isDir = true)
    )

    //then
    val resArgs = ArrayBuffer.empty[Seq[String]]
    childProcess.spawn.expects(
      "zip",
      *,
      *
    ).twice().onCall { (_, args, options) =>
      inside(options) { case Some(opts) =>
        opts.windowsHide shouldBe true
      }
      resArgs += args
      Future.successful(subProcess)
    }

    //when
    val resultF = for {
      _ <- api.delete(parent, items).toFuture
      res <- api.readDir(parent, js.undefined).toFuture
    } yield res

    //then
    resultF.map { res =>
      res.items.toList should be (empty)
      resArgs shouldBe List(
        List("-qd", zipPath, "dir 1/dir 2/file 2", "dir 1/dir 2/dir 3/"),
        List("-qd", zipPath, "dir 1/file 1", "dir 1/dir 2/")
      )
    }
  }

  it should "spawn zip command when addToZip" in {
    //given
    val stdout = new StreamReader(Readable.from(Buffer.from(
      """  adding: 1/ (stored 0%)
        |  adding: 1/2.txt (stored 1%)
        |  adding: 1/1.txt (stored 2.3%)
        |""".stripMargin)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
    val childProcess = new ChildProcess
    ZipApi.childProcess = childProcess.childProcess
    val parent = "test dir"
    val zipFile = "test.zip"
    val items = js.Set("item 1", "item 2")
    val onNextItem = mockFunction[Unit]

    //then
    childProcess.spawn.expects(
      "zip",
      List("-r", "test.zip", "item 1", "item 2"),
      *
    ).onCall { (_, _, options) =>
      inside(options) { case Some(opts) =>
        opts.cwd shouldBe parent
        opts.windowsHide shouldBe true
      }
      Future.successful(subProcess)
    }
    onNextItem.expects().repeat(3)

    //when
    val resultF = ZipApi.addToZip(zipFile, parent, items, onNextItem).toFuture

    //then
    resultF.map(_ => Succeeded)
  }

  it should "return empty map if empty zip when readZip" in {
    //given
    val expectedOutput =
      """Archive:  ./1.zip
        |Zip file size: 22 bytes, number of entries: 0
        |Empty zipfile.
        |""".stripMargin
    val stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)))
    val rawProcess = literal().asInstanceOf[raw.ChildProcess]
    val error = js.Error("sub-process exited with code=1")
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[js.UndefOr[js.Error]](error))
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
      res.toMap shouldBe Map.empty
    }
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
    val subProcess = SubProcess(rawProcess, stdout, js.Promise.resolve[Unit](js.undefined: Unit))
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
      assertEntries(res, Map(
        "test/dir" -> List(
          ZipEntry("test/dir", "file.txt", isDir = false, 1, js.Date.parse("2019-06-28T16:19:23"), "-rw-r--r--")
        ),
        "test" -> List(
          ZipEntry("test", "dir", isDir = true, 0, js.Date.parse("2019-06-28T16:19:23"), "drw-r--r--")
        ),
        "" -> List(
          ZipEntry("", "test", isDir = true, 0, js.Date.parse("2019-06-28T16:19:23"), "drw-r--r--")
        )
      ))
    }
  }
  
  private def assertEntries(result: js.Map[String, js.Array[FileListItem]], expected: Map[String, List[FileListItem]]): Assertion = {
    result.size shouldBe expected.size
    result.keySet shouldBe expected.keySet
    result.foreach { case (parent, resEntries) =>
      val expEntries = expected(parent)
      assertFileListItems(resEntries.toList, expEntries)
    }
    Succeeded
  }
}
