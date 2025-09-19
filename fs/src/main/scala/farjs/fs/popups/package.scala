package farjs.fs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../fs/popups/DriveController.mjs", JSImport.Default)
  object DriveController extends ReactClass

  @js.native
  @JSImport("../fs/popups/DrivePopup.mjs", JSImport.Default)
  object DrivePopup extends ReactClass

  @js.native
  @JSImport("../fs/popups/FoldersHistoryController.mjs", JSImport.Default)
  object FoldersHistoryController extends ReactClass

  @js.native
  @JSImport("../fs/popups/FoldersHistoryPopup.mjs", JSImport.Default)
  object FoldersHistoryPopup extends ReactClass

  @js.native
  @JSImport("../fs/popups/FolderShortcutsController.mjs", JSImport.Default)
  object FolderShortcutsController extends ReactClass

  @js.native
  @JSImport("../fs/popups/FolderShortcutsPopup.mjs", JSImport.Default)
  object FolderShortcutsPopup extends ReactClass
}
