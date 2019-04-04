package scommons.react.blessed.raw

import io.github.shogowada.scalajs.reactjs.elements.ReactElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("react-blessed", JSImport.Default)
object ReactBlessed extends js.Object {

  def render(element: ReactElement, screen: BlessedScreen): Unit = js.native
}
