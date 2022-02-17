package farjs.app

import farjs.filelist.FileListsState
import scommons.react.redux.task.AbstractTask

//noinspection NotImplementedCode
case class TestFarjsState(
                           currentTaskMock: () => Option[AbstractTask] = () => ???,
                           fileListsStateMock: () => FileListsState = () => ???
                         ) extends FarjsStateDef {

  def currentTask: Option[AbstractTask] = currentTaskMock()

  def fileListsState: FileListsState = fileListsStateMock()
}
