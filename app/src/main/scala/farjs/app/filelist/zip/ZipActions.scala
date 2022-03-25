package farjs.app.filelist.zip

import farjs.filelist.FileListActions

import scala.concurrent.Future

class ZipActions(protected val api: ZipApi) extends FileListActions {

  val isLocalFS: Boolean = false

  def getDriveRoot(path: String): Future[Option[String]] = Future.successful(None)
}
