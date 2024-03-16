package farjs.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait UiCharStartPos extends js.Object {

  val lcw: Int = js.native
  val pos: Int = js.native
  val rcw: Int = js.native
}

@js.native
sealed trait UiString extends js.Object {

  def strWidth(): Int = js.native

  def charStartPos(from: Int): UiCharStartPos = js.native

  def slice(from: Int, until: Int): String = js.native

  def ensureWidth(width: Int, padCh: Char): String = js.native
}

@js.native
@JSImport("@farjs/ui/UiString.mjs", JSImport.Default)
object UiString extends js.Function1[String, UiString] {

  def apply(str: String): UiString = js.native
}
