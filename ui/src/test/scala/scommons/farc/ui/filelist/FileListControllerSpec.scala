package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.react.test.TestSpec

class FileListControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val actions = mock[FileListActions]
    val controller = new FileListController(actions, isRight = false)
    
    //when & then
    controller.uiComponent shouldBe FileListPanel
  }
  
  it should "map state(left) to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListController(actions, isRight = false)
    val dispatch = mock[Dispatch]
    val fileListState = mock[FileListState]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarcStateDef]
    (state.fileListsState _).expects().returning(fileListsState)
    (fileListsState.left _).expects().returning(fileListState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPanelProps(disp, resActions, resFileListState) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      resFileListState shouldBe fileListState
    }
  }
  
  it should "map state(right) to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListController(actions, isRight = true)
    val dispatch = mock[Dispatch]
    val fileListState = mock[FileListState]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarcStateDef]
    (state.fileListsState _).expects().returning(fileListsState)
    (fileListsState.right _).expects().returning(fileListState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPanelProps(disp, resActions, resFileListState) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      resFileListState shouldBe fileListState
    }
  }
}
