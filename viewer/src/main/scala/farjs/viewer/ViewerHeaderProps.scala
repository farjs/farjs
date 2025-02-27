package farjs.viewer

import scala.scalajs.js

sealed trait ViewerHeaderProps extends js.Object {
  val filePath: String
  val encoding: String
  val size: Double
  val column: Int
  val percent: Int
}

object ViewerHeaderProps {

  def apply(filePath: String,
            encoding: String = "",
            size: Double = 0,
            column: Int = 0,
            percent: Int = 0): ViewerHeaderProps = {

    js.Dynamic.literal(
      filePath = filePath,
      encoding = encoding,
      size = size,
      column = column,
      percent = percent
    ).asInstanceOf[ViewerHeaderProps]
  }

  def unapply(arg: ViewerHeaderProps): Option[(String, String, Double, Int, Int)] = {
    Some((
      arg.filePath,
      arg.encoding,
      arg.size,
      arg.column,
      arg.percent
    ))
  }
}
