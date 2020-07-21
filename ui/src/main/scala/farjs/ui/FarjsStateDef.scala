package farjs.ui

import farjs.ui.filelist.FileListsStateDef
import scommons.react.redux.task.AbstractTask

trait FarjsStateDef {

  def currentTask: Option[AbstractTask]
  def fileListsState: FileListsStateDef
}
