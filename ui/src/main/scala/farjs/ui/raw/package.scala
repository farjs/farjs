package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object raw {

  @js.native
  @JSImport("@farjs/ui/UI.mjs", JSImport.Namespace)
  object NativeUI extends js.Object {

    def renderText2(style: BlessedStyle, text: String): String = js.native

    def renderText(isBold: Boolean, fgColor: String, bgColor: String, text: String): String = js.native
  }
}
