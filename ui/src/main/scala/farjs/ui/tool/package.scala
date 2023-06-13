package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object tool {

  @js.native
  @JSImport("@farjs/ui/tool/LogPanel.mjs", JSImport.Default)
  object LogPanel extends ReactClass
}
