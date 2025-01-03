package farjs.filelist

import farjs.filelist.FileListUi._
import farjs.filelist.stack.WithStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, WithStacksData, PanelStackItem}
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
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState()
    val onClose: js.Function0[Unit] = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
    ), null)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("otherComp".asInstanceOf[ReactClass])
    ), null)
    val fileListUi = new FileListUi(FileListUiData(data = Some(FileListData(dispatch, actions, state))))
    val uiData = fileListUi.data.copy(onClose = onClose)

    //when
    val result = createTestRenderer(
      withContext(<(fileListUi())(^.plain := props)(), WithStacksData(leftStack, null), WithStacksData(rightStack, null))
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
