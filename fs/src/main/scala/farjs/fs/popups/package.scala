package farjs.fs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../fs/popups/FolderShortcutsPopup.mjs", JSImport.Default)
  object FolderShortcutsPopup extends ReactClass
}
