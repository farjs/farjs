package farjs.ui

import farjs.ui.border.BorderExports
import scommons.react.ReactClass

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ui")
@JSExportAll
object Exports {

  val Button: ReactClass = farjs.ui.Button()

  val ButtonsPanel: ReactClass = farjs.ui.ButtonsPanel()

  val CheckBox: ReactClass = farjs.ui.CheckBox()

  val ProgressBar: ReactClass = farjs.ui.ProgressBar()

  val TextBox: ReactClass = farjs.ui.TextBox()

  val TextAlign: TextAlign = farjs.ui.TextAlign

  val TextLine: ReactClass = farjs.ui.TextLine()

  val WithSize: ReactClass = farjs.ui.WithSize()

  val border: BorderExports = farjs.ui.border.BorderExports
}
