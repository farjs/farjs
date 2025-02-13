package farjs.file

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../file/popups/EncodingsPopup.mjs", JSImport.Default)
  object EncodingsPopup extends ReactClass

  @js.native
  @JSImport("../file/popups/FileViewHistoryPopup.mjs", JSImport.Default)
  object FileViewHistoryPopup extends ReactClass

  @js.native
  @JSImport("../file/popups/TextSearchPopup.mjs", JSImport.Default)
  object TextSearchPopup extends ReactClass
}
