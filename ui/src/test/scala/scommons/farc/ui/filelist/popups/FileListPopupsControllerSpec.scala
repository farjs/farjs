package scommons.farc.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.farc.ui.filelist.FileListsStateDef
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
    val popupsState = mock[FileListPopupsState]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarcStateDef]
    (state.fileListsState _).expects().returning(fileListsState)
    (fileListsState.popups _).expects().returning(popupsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPopupsProps(disp, resPopupsState) =>
      disp shouldBe dispatch
      resPopupsState shouldBe popupsState
    }
  }
}
