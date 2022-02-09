package farjs.filelist.fs

import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.test.TestSpec

class FSControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val actions = new MockFileListActions
    val controller = new FSController(actions)
    
    //when & then
    controller.uiComponent shouldBe FSPanel
  }
  
  it should "map state to props" in {
    //given
    val actions = new MockFileListActions
    val props = mock[Props[Unit]]
    val controller = new FSController(actions)
    val dispatch = mockFunction[Any, Any]
    val fileListsState = mock[FileListsStateDef]
    val fileListsStateMock = mockFunction[FileListsStateDef]
    val state = new FileListsGlobalState {
      override def fileListsState: FileListsStateDef = fileListsStateMock()
    }
    fileListsStateMock.expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FSPanelProps(disp, resActions, data) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      data shouldBe fileListsState
    }
  }
}
