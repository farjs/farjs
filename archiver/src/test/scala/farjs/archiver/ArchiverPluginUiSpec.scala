package farjs.archiver

import farjs.archiver.ArchiverPluginUi._
import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.assertFileListItemCreatedAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.Dispatch
import farjs.ui.task.Task
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class ArchiverPluginUiSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ArchiverPluginUi.addToArchController = mockUiComponent("AddToArchController")

  //noinspection TypeAnnotation
  class Actions {
    val updateDir = mockFunction[Dispatch, String, FileListDirUpdateAction]

    val actions = new MockFileListActions(
      updateDirMock = updateDir
    )
  }

  it should "call onClose when onCancel" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val items = List(FileListItem("item 1"))
    val data = FileListData(dispatch, actions, FileListState(
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(items: _*))
    ))
    val pluginUi = new ArchiverPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(dispatch, onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val controller = findComponentProps(comp, addToArchController)
    
    //then
    onClose.expects()

    //when
    controller.onCancel()
    
    Succeeded
  }

  it should "dispatch actions when onComplete" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val items = List(
      FileListItem("item 2"),
      FileListItem("item 3")
    )
    val data = FileListData(dispatch, actions.actions, FileListState(
      index = 1,
      currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
        FileListItem.up,
        FileListItem("item 1")
      ) ++ items),
      selectedNames = js.Set("item 3", "item 2")
    ))
    val pluginUi = new ArchiverPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(dispatch, onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val controller = findComponentProps(comp, addToArchController)
    
    val zipFile = "test.zip"
    val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
      FileListItem("file 1")
    ))
    val updateAction = FileListDirUpdateAction(
      Task("Updating...", Future.successful(updatedDir))
    )
    
    //then
    onClose.expects()
    actions.updateDir.expects(dispatch, data.state.currDir.path).returning(updateAction)
    dispatch.expects(updateAction)
    dispatch.expects(*).onCall { action: Any =>
      assertFileListItemCreatedAction(action, FileListItemCreatedAction(zipFile, updatedDir))
    }
    
    //when
    controller.onComplete(zipFile)
    
    //then
    updateAction.task.result.toFuture.map(_ => Succeeded)
  }

  it should "render component" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val data = FileListData(dispatch, actions, state)
    val items = List(FileListItem("item 1"))
    val pluginUi = new ArchiverPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(dispatch, onClose)
    
    //when
    val result = createTestRenderer(<(pluginUi())(^.plain := props)()).root

    //then
    assertComponents(result.children, List(
      <(addToArchController())(^.assertWrapped(inside(_) {
        case AddToArchControllerProps(resDispatch, resActions, state, zipName, resItems, action, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          state shouldBe data.state
          zipName shouldBe "item 1.zip"
          resItems shouldBe items
          action shouldBe AddToArchAction.Add
      }))()
    ))
  }
}
