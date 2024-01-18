package farjs.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait ListViewport extends js.Object {
  
  val offset: Int = js.native

  val focused: Int = js.native

  val length: Int = js.native

  val viewLength: Int = js.native

  def updated(offset: Int, focused: js.UndefOr[Int] = js.native): ListViewport = js.native

  def down(): ListViewport = js.native

  def up(): ListViewport = js.native

  def pagedown(): ListViewport = js.native

  def pageup(): ListViewport = js.native

  def end(): ListViewport = js.native

  def home(): ListViewport = js.native

  def onKeypress(keyFull: String): js.UndefOr[ListViewport] = js.native

  def resize(newViewLength: Int): ListViewport = js.native
}

@js.native
@JSImport("@farjs/ui/ListViewport.mjs", "createListViewport")
object ListViewport extends js.Function3[Int, Int, Int, ListViewport] {

  def apply(index: Int, length: Int, viewLength: Int): ListViewport = js.native
}
