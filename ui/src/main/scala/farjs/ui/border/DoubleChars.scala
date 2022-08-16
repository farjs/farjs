package farjs.ui.border

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
sealed trait DoubleChars {

  // lines
  val horizontal = "\u2550"
  val vertical = "\u2551"

  // corners
  val topLeft = "\u2554"
  val topRight = "\u2557"
  val bottomLeft = "\u255a"
  val bottomRight = "\u255d"

  // connectors
  val top = "\u2566"
  val bottom = "\u2569"
  val left = "\u2560"
  val right = "\u2563"

  // single connectors
  val topSingle = "\u2564"
  val bottomSingle = "\u2567"
  val leftSingle = "\u255f"
  val rightSingle = "\u2562"

  // crosses
  val cross = "\u256c"
  val crossSingleVert = "\u256a"
  val crossSingleHoriz = "\u256b"
}

object DoubleChars extends DoubleChars
