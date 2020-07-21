package farjs.app

import farjs.ui.FarjsStateDef
import farjs.ui.filelist.{FileListsState, FileListsStateReducer}
import scommons.react.redux.task.{AbstractTask, TaskReducer}

case class FarjsState(currentTask: Option[AbstractTask],
                      fileListsState: FileListsState) extends FarjsStateDef

object FarjsStateReducer {

  def reduce(state: Option[FarjsState], action: Any): FarjsState = FarjsState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action),
    fileListsState = FileListsStateReducer(state.map(_.fileListsState), action)
  )
}
