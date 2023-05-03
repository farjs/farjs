package farjs.app.filelist.service

import farjs.text.{FileViewHistory, FileViewHistoryService}

import scala.concurrent.Future

class FileViewHistoryServiceImpl extends FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]] = Future.successful(Nil)

  def save(h: FileViewHistory): Future[Unit] = Future.unit
}
