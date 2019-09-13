package farclone.app

import farclone.ui.FarcStateDef
import farclone.ui.filelist.{FileListsStateReducer, FileListsState}
import scommons.react.redux.task.{AbstractTask, TaskReducer}

case class FarcState(currentTask: Option[AbstractTask],
                     fileListsState: FileListsState) extends FarcStateDef

object FarcStateReducer {

  def reduce(state: Option[FarcState], action: Any): FarcState = FarcState(
    currentTask = TaskReducer(state.flatMap(_.currentTask), action),
    fileListsState = FileListsStateReducer(state.map(_.fileListsState), action)
  )
}
