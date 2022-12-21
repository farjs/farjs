package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPluginUi._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.collection.immutable.ListSet
import scala.concurrent.Future

class ZipPluginUiSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ZipPluginUi.addToZipController = mockUiComponent("AddToZipController")

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
      currDir = FileListDir("/sub-dir", isRoot = false, items = items)
    ))
    val pluginUi = new ZipPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val controller = findComponentProps(comp, addToZipController)
    
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
      currDir = FileListDir("/sub-dir", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ) ++ items),
      selectedNames = ListSet("item 3", "item 2")
    ))
    val pluginUi = new ZipPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(onClose = onClose)
    val comp = testRender(<(pluginUi())(^.plain := props)())
    val controller = findComponentProps(comp, addToZipController)
    
    val zipFile = "test.zip"
    val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
      FileListItem("file 1")
    ))
    val updateAction = FileListDirUpdateAction(
      FutureTask("Updating...", Future.successful(updatedDir))
    )
    
    //then
    onClose.expects()
    actions.updateDir.expects(dispatch, data.state.currDir.path).returning(updateAction)
    dispatch.expects(FileListItemCreatedAction(zipFile, updatedDir))
    dispatch.expects(updateAction)
    
    //when
    controller.onComplete(zipFile)
    
    //then
    updateAction.task.future.map(_ => Succeeded)
  }

  it should "render component" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val data = FileListData(dispatch, actions, state)
    val items = List(FileListItem("item 1"))
    val pluginUi = new ZipPluginUi(data, "item 1.zip", items)
    val props = FileListPluginUiProps(onClose = onClose)
    
    //when
    val result = createTestRenderer(<(pluginUi())(^.plain := props)()).root

    //then
    assertComponents(result.children, List(
      <(addToZipController())(^.assertWrapped(inside(_) {
        case AddToZipControllerProps(resDispatch, resActions, state, zipName, resItems, action, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          state shouldBe data.state
          zipName shouldBe "item 1.zip"
          resItems shouldBe items
          action shouldBe AddToZipAction.Add
      }))()
    ))
  }
}
