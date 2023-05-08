package farjs.app.filelist.service

import farjs.text.{FileViewHistory, FileViewHistoryService}

import scala.concurrent.Future

class FileViewHistoryServiceImpl extends FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]] = Future.successful(Nil)

  def getOne(path: String, isEdit: Boolean): Future[Option[FileViewHistory]] = Future.successful(None)

  def save(h: FileViewHistory): Future[Unit] = Future.unit
}
