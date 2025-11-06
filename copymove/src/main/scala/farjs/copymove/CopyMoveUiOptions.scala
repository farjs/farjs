package farjs.copymove

import farjs.filelist.FileListData

import scala.scalajs.js

sealed trait CopyMoveUiOptions extends js.Object {
  val show: CopyMoveUiAction
  val from: FileListData
  val maybeTo: js.UndefOr[FileListData]
}

object CopyMoveUiOptions {

  def apply(show: CopyMoveUiAction,
            from: FileListData,
            maybeTo: js.UndefOr[FileListData]): CopyMoveUiOptions = {

    js.Dynamic.literal(
      show = show,
      from = from,
      maybeTo = maybeTo
    ).asInstanceOf[CopyMoveUiOptions]
  }

  def unapply(arg: CopyMoveUiOptions): Option[(CopyMoveUiAction, FileListData, js.UndefOr[FileListData])] = {
    Some((
      arg.show,
      arg.from,
      arg.maybeTo
    ))
  }
}
