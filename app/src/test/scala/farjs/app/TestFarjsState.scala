package farjs.app

import farjs.ui.task.AbstractTask

//noinspection NotImplementedCode
case class TestFarjsState(
                           currentTaskMock: () => Option[AbstractTask] = () => ???
                         ) extends FarjsStateDef {

  def currentTask: Option[AbstractTask] = currentTaskMock()
}
