package farjs.app.filelist

import farjs.app.TestFarjsState
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.test.TestSpec

class FileListControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val controller = FileListController
    
    //when & then
    controller.uiComponent shouldBe FileListBrowser
  }
  
  it should "map state to props" in {
    //given
    val props = mock[Props[Unit]]
    val controller = FileListController
    val dispatch = mockFunction[Any, Any]
    val state = TestFarjsState()

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FileListBrowserProps(disp, isRightInitiallyActive, plugins) =>
      disp shouldBe dispatch
      isRightInitiallyActive shouldBe false
      plugins should not be Nil
    }
  }
}
