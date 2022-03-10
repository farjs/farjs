package farjs.app.filelist.zip

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.react.test._

class ZipPanelSpec extends TestSpec with TestRendererUtils {

  ZipPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  it should "return false when onKeypress(unknown key)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState())
    val zipPanel = new ZipPanel("filePath.zip", onClose)
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //when & then
    panelProps.onKeypress(null, "unknown") shouldBe false
  }

  it should "call onClose if on .. in root dir when onKeypress(enter)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      currDir = FileListDir("ZIP://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("item 1")
      ))
    ))
    val zipPanel = new ZipPanel("filePath.zip", onClose)
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe true
  }

  it should "not call onClose if not on .. in root dir when onKeypress(enter)" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FileListPanelProps(dispatch, actions, FileListState(
      index = 1,
      currDir = FileListDir("ZIP://filePath.zip", isRoot = false, items = List(
        FileListItem.up,
        FileListItem("dir 1", isDir = true)
      ))
    ))
    val zipPanel = new ZipPanel("filePath.zip", onClose)
    val comp = testRender(<(zipPanel())(^.wrapped := props)())
    val panelProps = findComponentProps(comp, fileListPanelComp)

    //then
    onClose.expects().never()

    //when & then
    panelProps.onKeypress(null, "enter") shouldBe false
  }

  it should "render initial component" in {
    //given
    val onClose = mockFunction[Unit]
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    val props = FileListPanelProps(dispatch, actions, state)
    val zipPanel = new ZipPanel("filePath.zip", onClose)
    
    //when
    val result = testRender(<(zipPanel())(^.wrapped := props)())

    //then
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }
  }
}
