package farjs.ui.popup

import scommons.react.ReactClass

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
sealed trait PopupExports {

  val MessageBox: ReactClass = farjs.ui.popup.MessageBox()

  val MessageBoxAction: MessageBoxActionExports = farjs.ui.popup.MessageBoxAction
}

object PopupExports extends PopupExports
