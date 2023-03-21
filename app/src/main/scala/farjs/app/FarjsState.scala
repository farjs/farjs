package farjs.app

import farjs.filelist.{FileListsGlobalState, FileListsState, FileListsStateReducer}
import scommons.react.redux.task.{AbstractTask, TaskReducer}

trait FarjsStateDef extends FileListsGlobalState {

  def currentTask: Option[AbstractTask]
}

case class FarjsState(currentTask: Option[AbstractTask],
                      fileListsState: FileListsState) extends FarjsStateDef

object FarjsStateReducer {

  def reduce(state: Option[FarjsState], action: Any): FarjsState = FarjsState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action),
    fileListsState = FileListsStateReducer(state.map(_.fileListsState), action)
  )
}
