package farjs.filelist

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../filelist/popups/DeleteController.mjs", JSImport.Default)
  object DeleteController extends ReactClass

  @js.native
  @JSImport("../filelist/popups/HelpController.mjs", JSImport.Default)
  object HelpController extends ReactClass

  @js.native
  @JSImport("../filelist/popups/ExitController.mjs", JSImport.Default)
  object ExitController extends ReactClass

  @js.native
  @JSImport("../filelist/popups/MenuController.mjs", JSImport.Default)
  object MenuController extends ReactClass

  @js.native
  @JSImport("../filelist/popups/MakeFolderPopup.mjs", JSImport.Default)
  object MakeFolderPopup extends ReactClass

  @js.native
  @JSImport("../filelist/popups/MakeFolderController.mjs", JSImport.Default)
  object MakeFolderController extends ReactClass

  @js.native
  @JSImport("../filelist/popups/SelectPopup.mjs", JSImport.Default)
  object SelectPopup extends ReactClass

  @js.native
  @JSImport("../filelist/popups/SelectController.mjs", JSImport.Default)
  object SelectController extends ReactClass
}
