package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ui")
object Exports {

  @JSExport("Button")
  val button: ReactClass = Button()

  @JSExport("CheckBox")
  val checkBox: ReactClass = CheckBox()
}
