package farjs.ui.border

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
sealed trait SingleChars {

  // lines
  val horizontal = "\u2500"
  val vertical = "\u2502"

  // corners
  val topLeft = "\u250c"
  val topRight = "\u2510"
  val bottomLeft = "\u2514"
  val bottomRight = "\u2518"

  // connectors
  val top = "\u252c"
  val bottom = "\u2534"
  val left = "\u251c"
  val right = "\u2524"

  // double connectors
  val topDouble = "\u2565"
  val bottomDouble = "\u2568"
  val leftDouble = "\u255e"
  val rightDouble = "\u2561"

  // crosses
  val cross = "\u253c"
  val crossDoubleVert = "\u256b"
  val crossDoubleHoriz = "\u256a"
}

object SingleChars extends SingleChars
