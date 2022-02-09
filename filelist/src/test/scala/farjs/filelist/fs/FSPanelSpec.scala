package farjs.filelist.fs

import farjs.filelist.fs.FSPanel._
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.filelist._
import scommons.react.test._

class FSPanelSpec extends TestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  it should "render component with left state" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListsState()
    val props = FSPanelProps(dispatch, actions, state)
    val isRight = false
    
    //when
    val result = testRender(
      withContext(<(FSPanel())(^.wrapped := props)(), isRight = isRight)
    )

    //then
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state.left
    }
  }

  it should "render component with right state" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListsState()
    val props = FSPanelProps(dispatch, actions, state)
    val isRight = true
    
    //when
    val result = testRender(
      withContext(<(FSPanel())(^.wrapped := props)(), isRight = isRight)
    )

    //then
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state.right
    }
  }
}
