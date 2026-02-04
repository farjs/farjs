package farjs.archiver.zip

import farjs.archiver.ArchiverPlugin
import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.{assertFileListDirUpdatedAction, assertFileListDiskSpaceUpdatedAction}
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.task.TaskAction
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js

class ZipActionsSpec extends AsyncTestSpec {

  it should "re-create ZipApi when updateDir" in {
    //given
    val readZip = mockFunction[String, Future[Map[String, List[FileListItem]]]]
    val createApi = mockFunction[String, String, Future[Map[String, List[FileListItem]]], ZipApi]
    ArchiverPlugin.readZip = readZip
    ArchiverPlugin.createApi = createApi
    val actions = new ZipActions(new ZipApi("file.zip", "root.path", Future.successful(Map.empty)))
    val dispatch = mockFunction[js.Any, Unit]
    val currDir = FileListDir("/", isRoot = true, items = js.Array(FileListItem("file 1")))
    val path = "/test/path"
    val api = new ZipApi("file.zip", "root.path", Future.successful(Map.empty)) {
      override def readDir(path: String, dir: js.UndefOr[String]): js.Promise[FileListDir] = {
        js.Promise.resolve[FileListDir](currDir)
      }
    }
    val entriesByParent = Future.successful(Map(
      "" -> List(
        ZipEntry("", "file 1", size = 100),
        ZipEntry("", "dir 1", isDir = true)
      ),
      "dir 1" -> List(
        ZipEntry("dir 1", "file 2", size = 23)
      )
    ))

    //then
    readZip.expects("file.zip").returning(entriesByParent)
    createApi.expects("file.zip", "root.path", *).returning(api)
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDiskSpaceUpdatedAction(action, FileListDiskSpaceUpdatedAction(123.0))
      ()
    }
    dispatch.expects(*).onCall { action: Any =>
      assertFileListDirUpdatedAction(action, FileListDirUpdatedAction(currDir))
      ()
    }

    //when
    val TaskAction(task) =
      actions.updateDir(dispatch, path)

    //then
    actions.api shouldBe api
    task.message shouldBe "Updating Dir"
    task.result.toFuture.map(_ => Succeeded)
  }
}
