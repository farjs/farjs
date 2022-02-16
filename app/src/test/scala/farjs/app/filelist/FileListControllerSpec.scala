package farjs.app.filelist

import farjs.app.TestFarjsState
import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.ReactClass
import scommons.react.test.TestSpec

class FileListControllerSpec extends TestSpec {

  private val fileListPopups = "test_popups".asInstanceOf[ReactClass]

  ignore should "return component" in {
    //given
    val actions = mock[FileListActions]
    val controller = new FileListController(actions, fileListPopups)
    
    //when & then
    controller.uiComponent shouldBe new FileListBrowser(fileListPopups)
  }
  
  it should "map state to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListController(actions, fileListPopups)
    val dispatch = mockFunction[Any, Any]
    val fileListsState = mock[FileListsStateDef]
    val fileListsStateMock = mockFunction[FileListsStateDef]
    val state = TestFarjsState(fileListsStateMock = fileListsStateMock)
    fileListsStateMock.expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListBrowserProps(disp, data, plugins) =>
      disp shouldBe dispatch
      data shouldBe fileListsState
      plugins should not be Nil
    }
  }
}
