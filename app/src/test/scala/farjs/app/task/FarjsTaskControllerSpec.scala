package farjs.app.task

import farjs.ui.FarjsStateDef
import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.redux.task.{FutureTask, TaskManager, TaskManagerProps}
import scommons.react.test.TestSpec

import scala.concurrent.Future

class FarjsTaskControllerSpec extends TestSpec {

  it should "return component" in {
    //when & then
    FarjsTaskController.uiComponent shouldBe TaskManager
  }
  
  it should "map state to props" in {
    //given
    val props = mock[Props[Unit]]
    val dispatch = mock[Dispatch]
    val currentTask = Some(FutureTask("test task", Future.successful(())))
    val state = mock[FarjsStateDef]
    (state.currentTask _).expects().returning(currentTask)

    //when
    val result = FarjsTaskController.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case TaskManagerProps(task) =>
      task shouldBe currentTask
    }
  }
}
