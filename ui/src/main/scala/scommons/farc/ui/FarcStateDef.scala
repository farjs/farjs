package scommons.farc.ui

import scommons.farc.ui.filelist.FileListsStateDef
import scommons.react.redux.task.AbstractTask

trait FarcStateDef {

  def currentTask: Option[AbstractTask]
  def fileListsState: FileListsStateDef
}
