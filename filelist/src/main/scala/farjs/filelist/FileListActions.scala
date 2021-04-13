package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListApi, FileListDir, FileListItem}
import io.github.shogowada.scalajs.reactjs.redux.Action
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs.Process.Platform
import scommons.nodejs._
import scommons.react.redux.task.{FutureTask, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Success
import scala.util.control.NonFatal

trait FileListActions {

  protected def api: FileListApi

  private[filelist] var platform: Platform = process.platform
  private[filelist] var childProcess: ChildProcess = child_process

  def openInDefaultApp(parent: String, item: String): FileListOpenInDefaultAppAction = {
    val name =
      if (item == FileListItem.up.name) FileListDir.curr
      else item
    
    val (_, future) = childProcess.exec(
      command = {
        if (platform == Platform.darwin) s"""open "$name""""
        else if (platform == Platform.win32) s"""start "" "$name""""
        else s"""xdg-open "$name""""
      },
      options = Some(new raw.ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })
    )

    FileListOpenInDefaultAppAction(FutureTask("Opening default app", future))
  }
  
  def changeDir(dispatch: Dispatch,
                isRight: Boolean,
                parent: Option[String],
                dir: String): FileListDirChangeAction = {
    
    val future = api.readDir(parent, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(isRight, dir, currDir))
    }

    FileListDirChangeAction(FutureTask("Changing Dir", future))
  }

  def createDir(dispatch: Dispatch,
                isRight: Boolean,
                parent: String,
                dir: String,
                multiple: Boolean): FileListDirCreateAction = {

    val future = for {
      created <- api.mkDir(parent, dir, multiple)
      currDir <- api.readDir(parent)
    } yield {
      dispatch(FileListDirCreatedAction(isRight, created, currDir))
      ()
    }

    FileListDirCreateAction(FutureTask("Creating Dir", future))
  }

  def deleteItems(dispatch: Dispatch,
                  isRight: Boolean,
                  dir: String,
                  items: Seq[FileListItem]): FileListItemsDeleteAction = {
    
    val future = api.delete(dir, items).andThen {
      case Success(_) => dispatch(FileListItemsDeletedAction(isRight))
    }

    FileListItemsDeleteAction(FutureTask("Deleting Items", future))
  }

  def scanDirs(parent: String,
               items: Seq[FileListItem],
               onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    items.foldLeft(Future.successful(true)) { case (resF, item) =>
      resF.flatMap {
        case true if item.isDir =>
          api.readDir(Some(parent), item.name).flatMap { ls =>
            if (onNextDir(ls.path, ls.items)) scanDirs(ls.path, ls.items, onNextDir)
            else Future.successful(false)
          }
        case res => Future.successful(res)
      }
    }
  }

  def copyFile(srcDirs: List[String],
               file: FileListItem,
               dstDirs: List[String],
               onExists: FileListItem => Future[Option[Boolean]],
               onProgress: (String, String, Double) => Future[Boolean]): Future[Boolean] = {

    var srcPosition: Double = 0.0
    
    api.writeFile(dstDirs, file.name, { existing =>
      onExists(existing).andThen {
        case Success(Some(overwrite)) if !overwrite => srcPosition = existing.size
      }
    }).flatMap {
      case None => Future.successful(true)
      case Some(target) =>
        api.readFile(srcDirs, file, srcPosition).flatMap { source =>
          val buff = new Uint8Array(copyBufferBytes)

          def loop(): Future[Boolean] = {
            source.readNextBytes(buff).flatMap { bytesRead =>
              if (bytesRead == 0) target.setModTime(file).map(_ => true)
              else {
                target.writeNextBytes(buff, bytesRead).flatMap { position =>
                  onProgress(source.file, target.file, position).flatMap {
                    case true => loop()
                    case false => Future.successful(false)
                  }
                }
              }
            }
          }

          loop().transformWith { res =>
            source.close().recover {
              case NonFatal(ex) => println(s"Failed to close srcFile: ${source.file}, error: $ex")
            }.flatMap(_ => Future.fromTry(res))
          }
        }.transformWith { res =>
          target.close().recover {
            case NonFatal(ex) => println(s"Failed to close dstFile: ${target.file}, error: $ex")
          }.flatMap(_ => Future.fromTry(res))
        }.flatMap { res =>
          if (!res) target.delete().map(_ => res)
          else Future.successful(res)
        }
    }
  }
}

object FileListActions {
  
  private val copyBufferBytes: Int = 64 * 1024

  case class FileListActivateAction(isRight: Boolean) extends Action
  case class FileListParamsChangedAction(isRight: Boolean,
                                         offset: Int,
                                         index: Int,
                                         selectedNames: Set[String]) extends Action

  case class FileListOpenInDefaultAppAction(task: FutureTask[(js.Object, js.Object)]) extends TaskAction
  
  case class FileListDirChangeAction(task: FutureTask[FileListDir]) extends TaskAction
  case class FileListDirChangedAction(isRight: Boolean, dir: String, currDir: FileListDir) extends Action
  
  case class FileListDirCreateAction(task: FutureTask[Unit]) extends TaskAction
  case class FileListDirCreatedAction(isRight: Boolean, dir: String, currDir: FileListDir) extends Action
  
  case class FileListItemsDeleteAction(task: FutureTask[Unit]) extends TaskAction
  case class FileListItemsDeletedAction(isRight: Boolean) extends Action
  
  case class FileListScanDirsAction(task: FutureTask[Boolean]) extends TaskAction
  case class FileListItemsViewedAction(isRight: Boolean, sizes: Map[String, Double]) extends Action
}
