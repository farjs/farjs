package farjs.ui

import farjs.ui.border.BorderExports
import farjs.ui.popup.PopupExports
import scommons.react.ReactClass

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ui")
@JSExportAll
object Exports {

  val CheckBox: ReactClass = farjs.ui.CheckBox()

  val ComboBox: ReactClass = farjs.ui.ComboBox()

  val ListBox: ReactClass = farjs.ui.ListBox()

  val ProgressBar: ReactClass = farjs.ui.ProgressBar()

  val ScrollBar: ReactClass = farjs.ui.ScrollBar()

  val TextBox: ReactClass = farjs.ui.TextBox()

  val TextAlign: TextAlign = farjs.ui.TextAlign

  val TextLine: ReactClass = farjs.ui.TextLine()

  val WithSize: ReactClass = farjs.ui.WithSize()

  val border: BorderExports = farjs.ui.border.BorderExports

  val popup: PopupExports = farjs.ui.popup.PopupExports
}
