package farjs.filelist.api

import farjs.filelist.api.FileListApiSpec.TestFileListApi
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FileListApiSpec extends AsyncTestSpec {
  
  private val api = TestFileListApi

  it should "do nothing when delete" in {
    //when
    val resultF = api.delete("parent", List(FileListItem("test")))

    //then
    resultF.map(_ => Succeeded)
  }

  it should "do nothing when mkDirs" in {
    //when
    val resultF = api.mkDirs(List("test"))

    //then
    resultF.map(_ => Succeeded)
  }

  it should "do nothing when readFile" in {
    //when
    val resultF = for {
      source <- api.readFile(List("parent"), FileListItem("test"), 0)
      bytes <- source.readNextBytes(new Uint8Array(1))
      _ <- source.close()
    } yield (source, bytes)

    //then
    resultF.map { case (source, bytes) =>
      source.file shouldBe "test"
      bytes shouldBe 0
    }
  }

  it should "return None when writeFile" in {
    //given
    val onExists = mockFunction[FileListItem, Future[Option[Boolean]]]

    //then
    onExists.expects(*).never()
    
    //when
    val resultF = api.writeFile(List("parent"), "test.file", onExists)

    //then
    resultF.map(_ shouldBe None)
  }
}

object FileListApiSpec {
  
  object TestFileListApi extends FileListApi {

    def capabilities: Set[String] = Set.empty

    def readDir(parent: Option[String], dir: String): Future[FileListDir] =
      Future.successful(FileListDir(path = "", isRoot = false, items = js.Array()))

    def readDir(targetDir: String): Future[FileListDir] =
      Future.successful(FileListDir(path = "", isRoot = false, items = js.Array()))
  }
}
