package farjs.app.filelist.zip

import farjs.app.filelist.zip.AddToZipController._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class AddToZipControllerSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  AddToZipController.addToZipPopup = mockUiComponent("AddToZipPopup")

  it should "render popup and call addToZipApi" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val items = Set("item 3", "item 2")
    val onComplete = mockFunction[String, Unit]
    val onCancel = mockFunction[Unit]
    val props = AddToZipControllerProps(dispatch, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1"),
        FileListItem("item 2"),
        FileListItem("item 3")
      )),
      selectedNames = items
    ), zipName = "new.zip", items, onComplete, onCancel)
    val addToZipApi = mockFunction[String, String, Set[String], Future[Unit]]
    AddToZipController.addToZipApi = addToZipApi

    //when
    val renderer = createTestRenderer(<(AddToZipController())(^.wrapped := props)())

    //then
    inside(findComponentProps(renderer.root, addToZipPopup)) {
      case AddToZipPopupProps(zipName, onAdd, onCancel) =>
        zipName shouldBe "new.zip"
        onCancel shouldBe props.onCancel

        //given
        val zipFile = "test.zip"

        //then
        addToZipApi.expects(zipFile, props.state.currDir.path, items).returning(Future.unit)
        dispatch.expects(FileListParamsChangedAction(
          offset = 0,
          index = 1,
          selectedNames = Set.empty
        ))
        var resultAction: Any = null
        dispatch.expects(*).onCall { action: Any =>
          resultAction = action
        }
        onComplete.expects(zipFile)

        //when
        onAdd(zipFile)

        //then
        findComponents(renderer.root, addToZipPopup()) should be (empty)
        inside(resultAction) {
          case FileListTaskAction(FutureTask("Adding files to zip archive", future)) =>
            future.map(_ => Succeeded)
        }
    }
  }
}
