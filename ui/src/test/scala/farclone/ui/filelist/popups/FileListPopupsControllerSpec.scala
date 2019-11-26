package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import farclone.ui.FarcStateDef
import farclone.ui.filelist.{FileListActions, FileListsStateDef}
import scommons.react.test.TestSpec

class FileListPopupsControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val actions = mock[FileListActions]
    val controller = new FileListPopupsController(actions)
    
    //when & then
    controller.uiComponent shouldBe FileListPopups
  }
  
  it should "map state to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListPopupsController(actions)
    val dispatch = mock[Dispatch]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarcStateDef]
    (state.fileListsState _).expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPopupsProps(disp, resActions, resState) =>
      disp shouldBe dispatch
      resActions shouldBe actions
      resState shouldBe fileListsState
    }
  }
}
