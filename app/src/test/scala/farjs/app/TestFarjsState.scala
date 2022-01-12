package farjs.app

import farjs.filelist.FileListsStateDef
import scommons.react.redux.task.AbstractTask

//noinspection NotImplementedCode
case class TestFarjsState(
                           currentTaskMock: () => Option[AbstractTask] = () => ???,
                           fileListsStateMock: () => FileListsStateDef = () => ???
                         ) extends FarjsStateDef {

  def currentTask: Option[AbstractTask] = currentTaskMock()

  def fileListsState: FileListsStateDef = fileListsStateMock()
}
