package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import farclone.ui.FarcStateDef
import farclone.ui.filelist.FileListsStateDef
import scommons.react.test.TestSpec

class FileListPopupsControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val controller = FileListPopupsController
    
    //when & then
    controller.uiComponent shouldBe FileListPopups
  }
  
  it should "map state to props" in {
    //given
    val props = mock[Props[Unit]]
    val controller = FileListPopupsController
    val dispatch = mock[Dispatch]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarcStateDef]
    (state.fileListsState _).expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPopupsProps(disp, resState) =>
      disp shouldBe dispatch
      resState shouldBe fileListsState
    }
  }
}
