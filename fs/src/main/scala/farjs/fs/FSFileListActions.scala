package farjs.fs

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FSFileListActions private[fs](
  val api: FileListApi,
  fsService: FSService
) extends FileListActions {

  val isLocalFS: Boolean = true

  def getDriveRoot(path: String): Future[Option[String]] = {
    fsService.readDisk(path).map(_.map(_.root))
  }
}

object FSFileListActions extends FSFileListActions(new FSFileListApi, FSService.instance)
