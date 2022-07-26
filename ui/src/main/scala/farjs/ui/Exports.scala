package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ui")
object Exports {

  @JSExport("Button")
  val button: ReactClass = Button()

  @JSExport("ButtonsPanel")
  val buttonsPanel: ReactClass = ButtonsPanel()

  @JSExport("CheckBox")
  val checkBox: ReactClass = CheckBox()

  @JSExport("ProgressBar")
  val progressBar: ReactClass = ProgressBar()
}
