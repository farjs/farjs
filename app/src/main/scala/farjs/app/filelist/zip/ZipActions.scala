package farjs.app.filelist.zip

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListApi

import scala.concurrent.Future

class ZipActions(filePath: String) extends FileListActions {

  protected val api: FileListApi = new ZipApi(filePath)

  def getDriveRoot(path: String): Future[Option[String]] = Future.successful(None)
}
