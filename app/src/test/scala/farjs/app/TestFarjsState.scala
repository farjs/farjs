package farjs.app

import farjs.filelist.FileListsState
import farjs.fs.popups.FSPopupsState
import scommons.react.redux.task.AbstractTask

//noinspection NotImplementedCode
case class TestFarjsState(
                           currentTaskMock: () => Option[AbstractTask] = () => ???,
                           fileListsStateMock: () => FileListsState = () => ???,
                           fsPopupsMock: () => FSPopupsState = () => ???
                         ) extends FarjsStateDef {

  def currentTask: Option[AbstractTask] = currentTaskMock()

  def fileListsState: FileListsState = fileListsStateMock()
  
  def fsPopups: FSPopupsState = fsPopupsMock()
}
