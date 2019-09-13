package farclone.ui

import farclone.ui.filelist.FileListsStateDef
import scommons.react.redux.task.AbstractTask

trait FarcStateDef {

  def currentTask: Option[AbstractTask]
  def fileListsState: FileListsStateDef
}
