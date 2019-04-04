package scommons.react.blessed.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("blessed", JSImport.Default)
object Blessed extends js.Object {

  def screen(config: BlessedScreenConfig): BlessedScreen = js.native
}

@js.native
trait BlessedScreen extends js.Object {
  
  def key(keys: js.Array[String], onKey: js.Function2[js.Object, js.Object, Unit]): Unit = js.native
}

@js.native
trait BlessedElement extends js.Object {
  
  val width: Int = js.native
  val height: Int = js.native
}
