package farjs.filelist.popups

import farjs.filelist._
import farjs.filelist.popups.FileListPopups._
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.test._

class FileListPopupsSpec extends TestSpec with TestRendererUtils {

  FileListPopups.helpController = mockUiComponent("HelpController")
  FileListPopups.exitController = mockUiComponent("ExitController")
  FileListPopups.menuController = mockUiComponent("MenuController")
  FileListPopups.deleteController = mockUiComponent("DeleteController")
  FileListPopups.makeFolderController = mockUiComponent("MakeFolderController")
  FileListPopups.selectController = mockUiComponent("SelectController")

  it should "render component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPopupsProps(dispatch, FileListPopupsState())
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(FileListPopups())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    assertComponents(result.children, List(
      <(helpController())(^.wrapped := props)(),
      <(exitController())(^.wrapped := props)(),
      <(menuController())(^.wrapped := props)(),
      
      <(deleteController())(^.assertWrapped(inside(_) {
        case PopupControllerProps(Some(FileListData(`dispatch`, `actions`, `state`)), props.popups) =>
      }))(),
      <(makeFolderController())(^.assertWrapped(inside(_) {
        case PopupControllerProps(Some(FileListData(`dispatch`, `actions`, `state`)), props.popups) =>
      }))(),
      <(selectController())(^.assertWrapped(inside(_) {
        case PopupControllerProps(Some(FileListData(`dispatch`, `actions`, `state`)), props.popups) =>
      }))()
    ))
  }
}
