package farjs.app.filelist

import farjs.app.TestFarjsState
import farjs.fs.popups.{FSPopups, FSPopupsProps, FSPopupsState}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.redux.Dispatch
import scommons.react.test.TestSpec

class FSPopupsControllerSpec extends TestSpec {

  it should "return component" in {
    //given
    val controller = FSPopupsController
    
    //when & then
    controller.uiComponent shouldBe FSPopups
  }
  
  it should "map state to props" in {
    //given
    val props = mock[Props[Unit]]
    val controller = FSPopupsController
    val dispatch = mock[Dispatch]
    val fsPopupsState = FSPopupsState()
    val fsPopupsMock = mockFunction[FSPopupsState]
    val state = TestFarjsState(fsPopupsMock = fsPopupsMock)
    fsPopupsMock.expects().returning(fsPopupsState)

    //when
    val result = controller.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case FSPopupsProps(resDispatch, popups) =>
      resDispatch shouldBe dispatch
      popups shouldBe fsPopupsState
    }
  }
}
