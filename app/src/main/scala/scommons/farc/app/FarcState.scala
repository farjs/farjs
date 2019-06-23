package scommons.farc.app

import scommons.farc.ui.FarcStateDef
import scommons.farc.ui.filelist.{FileListsStateReducer, FileListsState}
import scommons.react.redux.task.{AbstractTask, TaskReducer}

case class FarcState(currentTask: Option[AbstractTask],
                     fileListsState: FileListsState) extends FarcStateDef

object FarcStateReducer {

  def reduce(state: Option[FarcState], action: Any): FarcState = FarcState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action),
    fileListsState = FileListsStateReducer(state.map(_.fileListsState), action)
  )
}
