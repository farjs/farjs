package farjs.file

import farjs.filelist.history.{History, HistoryKind}

case class FileViewHistory(path: String, params: FileViewHistoryParams)

object FileViewHistory {

  val fileViewsHistoryKind: HistoryKind = HistoryKind("farjs.fileViews", 150)
  
  def toHistory(h: FileViewHistory): History = {
    History(h.path, h.params)
  }

  def fromHistory(h: History): Option[FileViewHistory] = {
    h.params.toOption.map { params =>
      FileViewHistory(h.item, params.asInstanceOf[FileViewHistoryParams])
    }
  }
}
