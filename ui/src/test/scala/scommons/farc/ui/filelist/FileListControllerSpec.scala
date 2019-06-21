package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.react.test.TestSpec

class FileListControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val actions = mock[FileListActions]
    val controller = new FileListController(actions)
    
    //when & then
    controller.uiComponent shouldBe FilePanel
  }
  
  it should "map state to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListController(actions)
    val dispatch = mock[Dispatch]
    val fileListState = mock[FileListState]
    val state = mock[FarcStateDef]
    (state.fileListState _).expects().returning(fileListState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FilePanelProps(disp, resActions, resFileListState) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      resFileListState shouldBe fileListState
    }
  }
}
