package farjs.app.filelist.fs.popups

import scommons.react.redux.Action

object FSPopupsActions {

  case class FoldersHistoryPopupAction(show: Boolean) extends Action
  case class FolderShortcutsPopupAction(show: Boolean) extends Action
}
