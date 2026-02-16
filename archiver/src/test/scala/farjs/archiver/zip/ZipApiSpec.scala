package farjs.archiver.zip

import farjs.filelist.MockChildProcess
import farjs.filelist.api.FileListItemSpec.assertFileListItems
import farjs.filelist.api._
import farjs.filelist.util.ChildProcess.ChildProcessOptions
import farjs.filelist.util.{StreamReader, SubProcess}
import scommons.nodejs._
import scommons.nodejs.raw.CreateReadStreamOptions
import scommons.nodejs.stream.Readable
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.typedarray.Uint8Array

class ZipApiSpec extends AsyncTestSpec {

  private val entriesByParentF = js.Promise.resolve[js.Map[String, js.Array[FileListItem]]](new js.Map[String, js.Array[FileListItem]](js.Array(
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
      override def extract(zipPath: String, filePath: String): js.Promise[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        js.Promise.resolve[SubProcess](subProcess)
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
      override def extract(zipPath: String, filePath: String): js.Promise[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        js.Promise.resolve[SubProcess](subProcess)
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
      override def extract(zipPath: String, filePath: String): js.Promise[SubProcess] = {
        zipPath shouldBe "/dir/filePath.zip"
        filePath shouldBe expectedFilePath
        js.Promise.resolve[SubProcess](subProcess)
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
}
