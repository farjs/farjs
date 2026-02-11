package farjs.archiver.zip

import farjs.archiver.ArchiverPlugin
import farjs.filelist.FileListActions
import farjs.filelist.FileListActions._
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.util.Success

class ZipActions(zipApi: ZipApi) extends FileListActions(zipApi) {

  override def updateDir(dispatch: Dispatch, path: String): TaskAction = {
    val entriesByParentF = ArchiverPlugin.readZip(zipApi.zipPath).toFuture.andThen {
      case Success(entries) =>
        val totalSize = entries.foldLeft(0.0) { (total, entry) =>
          total + entry._2.foldLeft(0.0)(_ + _.size)
        }
        dispatch(FileListDiskSpaceUpdatedAction(totalSize))
    }.toJSPromise
    api = ArchiverPlugin.createApi(zipApi.zipPath, zipApi.rootPath, entriesByParentF)
    
    val future = entriesByParentF.toFuture.flatMap(_ => api.readDir(path, js.undefined).toFuture).andThen {
      case Success(currDir) => dispatch(FileListDirUpdatedAction(currDir))
    }

    TaskAction(Task("Updating Dir", future))
  }
}
