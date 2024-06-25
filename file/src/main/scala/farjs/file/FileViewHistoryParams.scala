package farjs.file

import scala.scalajs.js

sealed trait FileViewHistoryParams extends js.Object {
  val isEdit: Boolean
  val encoding: String
  val position: Double
  val wrap: js.UndefOr[Boolean]
  val column: js.UndefOr[Int]
}

object FileViewHistoryParams {

  def apply(isEdit: Boolean,
            encoding: String,
            position: Double,
            wrap: js.UndefOr[Boolean],
            column: js.UndefOr[Int]): FileViewHistoryParams = {

    js.Dynamic.literal(
      isEdit = isEdit,
      encoding = encoding,
      position = position,
      wrap = wrap,
      column = column
    ).asInstanceOf[FileViewHistoryParams]
  }

  def unapply(arg: FileViewHistoryParams): Option[(Boolean, String, Double, js.UndefOr[Boolean], js.UndefOr[Int])] = {
    Some((
      arg.isEdit,
      arg.encoding,
      arg.position,
      arg.wrap,
      arg.column
    ))
  }

  def copy(p: FileViewHistoryParams)(isEdit: Boolean = p.isEdit,
                                     encoding: String = p.encoding,
                                     position: Double = p.position,
                                     wrap: js.UndefOr[Boolean] = p.wrap,
                                     column: js.UndefOr[Int] = p.column): FileViewHistoryParams = {

    FileViewHistoryParams(
      isEdit = isEdit,
      encoding = encoding,
      position = position,
      wrap = wrap,
      column = column
    )
  }
}
