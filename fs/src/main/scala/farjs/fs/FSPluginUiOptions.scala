package farjs.fs

import scala.scalajs.js

sealed trait FSPluginUiOptions extends js.Object {
  val showDrivePopupOnLeft: js.UndefOr[Boolean]
  val showFoldersHistoryPopup: Boolean
  val showFolderShortcutsPopup: Boolean
}

object FSPluginUiOptions {

  def apply(showDrivePopupOnLeft: js.UndefOr[Boolean], showFoldersHistoryPopup: Boolean, showFolderShortcutsPopup: Boolean): FSPluginUiOptions = {
    js.Dynamic.literal(
      showDrivePopupOnLeft = showDrivePopupOnLeft,
      showFoldersHistoryPopup = showFoldersHistoryPopup,
      showFolderShortcutsPopup = showFolderShortcutsPopup
    ).asInstanceOf[FSPluginUiOptions]
  }

  def unapply(arg: FSPluginUiOptions): Option[(Option[Boolean], Boolean, Boolean)] = {
    Some((
      arg.showDrivePopupOnLeft.toOption,
      arg.showFoldersHistoryPopup,
      arg.showFolderShortcutsPopup
    ))
  }
}
