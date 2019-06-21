package scommons.farc.app

import scommons.farc.ui.FarcStateDef
import scommons.farc.ui.filelist.{FileListState, FileListStateReducer}
import scommons.react.redux.task.{AbstractTask, TaskReducer}

case class FarcState(currentTask: Option[AbstractTask],
                     fileListState: FileListState) extends FarcStateDef

object FarcStateReducer {

  def reduce(state: Option[FarcState], action: Any): FarcState = FarcState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action),
    fileListState = FileListStateReducer(state.map(_.fileListState), action)
  )
}
