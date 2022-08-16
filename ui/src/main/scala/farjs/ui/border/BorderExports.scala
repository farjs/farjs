package farjs.ui.border

import scommons.react.ReactClass

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
sealed trait BorderExports {

  val DoubleBorder: ReactClass = farjs.ui.border.DoubleBorder()

  val DoubleChars: DoubleChars = farjs.ui.border.DoubleChars
}

object BorderExports extends BorderExports
