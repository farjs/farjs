package farjs.archiver.zip

import farjs.archiver.ArchiverPlugin
import farjs.filelist.FileListActions
import farjs.filelist.FileListActions._
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Success

class ZipActions(var api: ZipApi) extends FileListActions {

  val isLocalFS: Boolean = false

  def getDriveRoot(path: String): Future[Option[String]] = Future.successful(None)

  override def updateDir(dispatch: Dispatch, path: String): TaskAction = {
    val entriesByParentF = ArchiverPlugin.readZip(api.zipPath).andThen {
      case Success(entries) =>
        val totalSize = entries.foldLeft(0.0) { (total, entry) =>
          total + entry._2.foldLeft(0.0)(_ + _.size)
        }
        dispatch(FileListDiskSpaceUpdatedAction(totalSize))
    }
    api = ArchiverPlugin.createApi(api.zipPath, api.rootPath, entriesByParentF)
    
    val future = entriesByParentF.flatMap(_ => api.readDir(path, js.undefined).toFuture).andThen {
      case Success(currDir) => dispatch(FileListDirUpdatedAction(currDir))
    }

    TaskAction(Task("Updating Dir", future))
  }
}
