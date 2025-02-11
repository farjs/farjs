package farjs.file

import farjs.filelist.history.{History, HistoryKind}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait FileViewHistory extends js.Object {
  val path: String
  val params: FileViewHistoryParams
}

@js.native
@JSImport("../file/FileViewHistory.mjs", JSImport.Default)
object NativeFileViewHistory extends js.Object {

  val fileViewsHistoryKind: HistoryKind = js.native

  def toHistory(h: FileViewHistory): History = js.native

  def fromHistory(h: History): js.UndefOr[FileViewHistory] = js.native

  def pathToItem(path: String, isEdit: Boolean): String = js.native
}

object FileViewHistory {

  def apply(path: String, params: FileViewHistoryParams): FileViewHistory = {
    js.Dynamic.literal(
      path = path,
      params = params
    ).asInstanceOf[FileViewHistory]
  }

  def unapply(arg: FileViewHistory): Option[(String, FileViewHistoryParams)] = {
    Some((
      arg.path,
      arg.params
    ))
  }
  
  val fileViewsHistoryKind: HistoryKind = NativeFileViewHistory.fileViewsHistoryKind
  
  def toHistory(h: FileViewHistory): History =
    NativeFileViewHistory.toHistory(h)

  def fromHistory(h: History): js.UndefOr[FileViewHistory] =
    NativeFileViewHistory.fromHistory(h)
  
  def pathToItem(path: String, isEdit: Boolean): String =
    NativeFileViewHistory.pathToItem(path, isEdit)
}
