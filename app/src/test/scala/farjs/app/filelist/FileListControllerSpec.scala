package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.test.TestSpec

class FileListControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val actions = mock[FileListActions]
    val controller = new FileListController(actions)
    
    //when & then
    controller.uiComponent shouldBe FileListBrowser
  }
  
  it should "map state to props" in {
    //given
    val actions = mock[FileListActions]
    val props = mock[Props[Unit]]
    val controller = new FileListController(actions)
    val dispatch = mock[Dispatch]
    val fileListsState = mock[FileListsStateDef]
    val state = mock[FarjsStateDef]
    (state.fileListsState _).expects().returning(fileListsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListBrowserProps(disp, resActions, data, plugins) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      data shouldBe fileListsState
      plugins shouldBe Nil
    }
  }
}
