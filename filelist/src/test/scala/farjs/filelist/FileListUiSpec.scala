package farjs.filelist

import farjs.filelist.FileListUi._
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FileListUiSpec extends TestSpec with TestRendererUtils {

  FileListUi.helpController = mockUiComponent("HelpController")
  FileListUi.exitController = mockUiComponent("ExitController")
  FileListUi.menuController = mockUiComponent("MenuController")
  FileListUi.deleteController = mockUiComponent("DeleteController")
  FileListUi.makeFolderController = mockUiComponent("MakeFolderController")
  FileListUi.selectController = mockUiComponent("SelectController")

  it should "render component" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val onClose: js.Function0[Unit] = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val fileListUi = new FileListUi(FileListUiData(data = Some(FileListData(dispatch, actions, state))))
    val uiData = fileListUi.data.copy(onClose = onClose)

    //when
    val result = createTestRenderer(
      withContext(<(fileListUi())(^.plain := props)(), leftStack, rightStack)
    ).root

    //then
    assertComponents(result.children, List(
      <(helpController())(^.assertWrapped(_ shouldBe uiData))(),
      <(exitController())(^.assertWrapped(_ shouldBe uiData))(),
      <(menuController())(^.assertWrapped(_ shouldBe uiData))(),
      <(deleteController())(^.assertWrapped(_ shouldBe uiData))(),
      <(makeFolderController())(^.assertWrapped(_ shouldBe uiData))(),
      <(selectController())(^.assertWrapped(_ shouldBe uiData))()
    ))
  }
}
