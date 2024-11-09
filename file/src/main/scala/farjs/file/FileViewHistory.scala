package farjs.file

import farjs.filelist.history.{History, HistoryKind}

case class FileViewHistory(path: String, params: FileViewHistoryParams)

object FileViewHistory {

  val fileViewsHistoryKind: HistoryKind = HistoryKind("farjs.fileViews", 150)
  
  def toHistory(h: FileViewHistory): History = {
    History(
      item = pathToItem(h.path, h.params.isEdit),
      params = h.params
    )
  }

  def fromHistory(h: History): Option[FileViewHistory] = {
    h.params.toOption.map { params =>
      FileViewHistory(
        path = itemToPath(h.item),
        params = params.asInstanceOf[FileViewHistoryParams]
      )
    }
  }
  
  def pathToItem(path: String, isEdit: Boolean): String = {
    if (isEdit) s"E:$path"
    else s"V:$path"
  }

  def itemToPath(item: String): String = {
    if (item.startsWith("V:")) item.stripPrefix("V:")
    else if (item.startsWith("E:")) item.stripPrefix("E:")
    else item
  }
}
