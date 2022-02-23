package scommons.react.blessed.raw

import scommons.nodejs.raw.EventEmitter
import scommons.react.blessed.TerminalName

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/blessed", JSImport.Default)
object Blessed extends js.Object {

  def screen(config: BlessedScreenConfig): BlessedScreen = js.native
  
  def escape(text: String): String = js.native
}

@js.native
trait BlessedEventEmitter extends EventEmitter {

  def off(eventName: String, listener: js.Function): BlessedEventEmitter = js.native
}

@js.native
trait BlessedProgram extends BlessedEventEmitter {
  
  def showCursor(): Unit = js.native
  def hideCursor(): Unit = js.native
  
  def omove(x: Int, y: Int): Unit = js.native //optimized cursor move
}

@js.native
trait BlessedScreen extends BlessedEventEmitter {
  
  val program: BlessedProgram = js.native
  val terminal: TerminalName = js.native
  val cursor: BlessedCursor = js.native
  
  val focused: BlessedElement = js.native
  
  def focusPrevious(): Unit = js.native
  def focusNext(): Unit = js.native
  
  def key(keys: js.Array[String], onKey: js.Function2[js.Object, KeyboardKey, Unit]): Unit = js.native
  
  def cursorShape(shape: String, blink: Boolean): Boolean = js.native
  def destroy(): Unit = js.native
  def render(): Unit = js.native
  
  def copyToClipboard(text: String): Boolean = js.native
}

@js.native
trait BlessedCursor extends js.Object {

  val shape: String = js.native //block, underline, or line
  val blink: Boolean = js.native
}

@js.native
trait BlessedElement extends BlessedEventEmitter {
  
  val width: Int = js.native  //Calculated width
  val height: Int = js.native //Calculated height
  val left: Int = js.native   //Calculated relative left offset
  val top: Int = js.native    //Calculated relative top offset
  val aleft: Int = js.native  //Calculated absolute left offset
  val atop: Int = js.native   //Calculated absolute top offset
  
  val screen: BlessedScreen = js.native
  
  def focus(): Unit = js.native
}

@js.native
trait MouseData extends js.Object {

  val button: String = js.native
  val x: Int = js.native
  val y: Int = js.native
  val shift: Boolean = js.native
  val ctrl: Boolean = js.native
  val meta: Boolean = js.native
}

@js.native
trait KeyboardKey extends js.Object {
  
  val name: String = js.native
  val full: String = js.native
  
  val shift: js.UndefOr[Boolean] = js.native
  val ctrl: js.UndefOr[Boolean] = js.native
  val meta: js.UndefOr[Boolean] = js.native
  
  var defaultPrevented: js.UndefOr[Boolean] = js.native
}
