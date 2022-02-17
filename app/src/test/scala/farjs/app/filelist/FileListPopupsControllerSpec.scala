package farjs.app.filelist

import farjs.app.TestFarjsState
import farjs.filelist.FileListsState
import farjs.filelist.popups.{FileListPopups, FileListPopupsProps}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.redux.Dispatch
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
    val fileListsState = FileListsState()
    val fileListsStateMock = mockFunction[FileListsState]
    val state = TestFarjsState(fileListsStateMock = fileListsStateMock)
    fileListsStateMock.expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListPopupsProps(resDispatch, popups) =>
      resDispatch shouldBe dispatch
      popups shouldBe fileListsState.popups
    }
  }
}
