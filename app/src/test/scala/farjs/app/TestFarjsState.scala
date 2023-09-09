package farjs.app

import farjs.ui.task.Task

//noinspection NotImplementedCode
case class TestFarjsState(
                           currentTaskMock: () => Option[Task] = () => ???
                         ) extends FarjsStateDef {

  def currentTask: Option[Task] = currentTaskMock()
}
