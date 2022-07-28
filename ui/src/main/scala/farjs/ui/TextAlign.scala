package farjs.ui

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
sealed trait TextAlign {

  val left = "left"
  val right = "right"
  val center = "center"
}

object TextAlign extends TextAlign
