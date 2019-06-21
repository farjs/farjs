package scommons.farc.ui

import scommons.farc.ui.filelist.FileListState
import scommons.react.redux.task.AbstractTask

trait FarcStateDef {

  def currentTask: Option[AbstractTask]
  def fileListState: FileListState
}
