package farjs.filelist.stack

import farjs.filelist.FileListState
import scommons.nodejs.test.TestSpec
import scommons.react.ReactClass

import scala.scalajs.js

class PanelStackItemSpec extends TestSpec {

  it should "initialize dispatch when initDispatch" in {
    //given
    val parentDispatch = mockFunction[js.Any, Unit]
    val reducer = mockFunction[FileListState, Any, FileListState]
    val currState = FileListState(isActive = true)
    val item = PanelStackItem[FileListState]("test".asInstanceOf[ReactClass], None, None, Some(currState))
    var stackData = List[PanelStackItem[_]](item)
    val stack = new PanelStack(isActive = true, stackData, { f =>
      stackData = f(stackData)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when
    val result = PanelStackItem.initDispatch(parentDispatch, reducer, stack, item)

    //then
    inside(result) { case PanelStackItem(component, Some(dispatch), None, state) =>
      state shouldBe Some(currState)

      //given
      val action = "test action"
      val updatedState = FileListState()
      currState should not be updatedState

      //then
      reducer.expects(currState, action).returning(updatedState)
      parentDispatch.expects(action.asInstanceOf[js.Any])

      //when
      dispatch(action)

      //then
      inside(stackData.head) {
        case PanelStackItem(resComponent, None, None, resState) =>
          resComponent should be theSameInstanceAs component
          resState shouldBe Some(updatedState)
      }
    }
  }
}
