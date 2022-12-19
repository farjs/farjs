package farjs.app.filelist.fs.popups

import scommons.react.redux.Action

object FSPopupsActions {

  case class DrivePopupAction(show: DrivePopupShow) extends Action
  case class FoldersHistoryPopupAction(show: Boolean) extends Action
  case class FolderShortcutsPopupAction(show: Boolean) extends Action

  sealed trait DrivePopupShow
  case object DrivePopupHidden extends DrivePopupShow
  case object ShowDriveOnLeft extends DrivePopupShow
  case object ShowDriveOnRight extends DrivePopupShow
}
