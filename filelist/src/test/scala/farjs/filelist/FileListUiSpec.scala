package farjs.filelist

import farjs.filelist.FileListUi._
import farjs.filelist.stack.WithStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem, WithStacksData}
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class FileListUiSpec extends TestSpec with TestRendererUtils {

  FileListUi.helpController = "HelpController".asInstanceOf[ReactClass]
  FileListUi.exitController = "ExitController".asInstanceOf[ReactClass]
  FileListUi.menuController = "MenuController".asInstanceOf[ReactClass]
  FileListUi.deleteController = "DeleteController".asInstanceOf[ReactClass]
  FileListUi.makeFolderController = "MakeFolderController".asInstanceOf[ReactClass]
  FileListUi.selectController = "SelectController".asInstanceOf[ReactClass]

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
    val fileListUi = new FileListUi(FileListUiData(data = FileListData(dispatch, actions, state)))
    val uiData = FileListUiData.copy(fileListUi.data)(onClose = onClose)

    //when
    val result = createTestRenderer(
      withContext(<(fileListUi())(^.plain := props)(), WithStacksData(leftStack, null), WithStacksData(rightStack, null))
    ).root

    //then
    assertComponents(result.children, List(
      <(helpController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))(),
      <(exitController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))(),
      <(menuController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))(),
      <(deleteController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))(),
      <(makeFolderController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))(),
      <(selectController)(^.assertPlain[FileListUiData](assertFileListUiData(_, uiData)))()
    ))
  }
  
  private def assertFileListUiData(result: FileListUiData, expected: FileListUiData): Assertion = {
    inside(result) {
      case FileListUiData(onClose, data, showHelpPopup, showExitPopup, showMenuPopup, showDeletePopup, showMkFolderPopup, showSelectPopup) =>
        onClose shouldBe expected.onClose
        data shouldBe expected.data
        showHelpPopup shouldBe expected.showHelpPopup
        showExitPopup shouldBe expected.showExitPopup
        showMenuPopup shouldBe expected.showMenuPopup
        showDeletePopup shouldBe expected.showDeletePopup
        showMkFolderPopup shouldBe expected.showMkFolderPopup
        showSelectPopup shouldBe expected.showSelectPopup
    }
  }
}
