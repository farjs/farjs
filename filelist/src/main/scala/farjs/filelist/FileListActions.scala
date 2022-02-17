package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListApi, FileListDir, FileListItem}
import farjs.filelist.fs.FSService
import scommons.nodejs.{path => nodePath}
import scommons.react.redux._
import scommons.react.redux.task.{FutureTask, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Success
import scala.util.control.NonFatal

trait FileListActions {

  protected def api: FileListApi

  private[filelist] var fsService: FSService = FSService.instance

  def openInDefaultApp(parent: String, item: String): FileListTaskAction = {
    val future = fsService.openItem(parent, item)
    
    FileListTaskAction(FutureTask("Opening default app", future))
  }
  
  def changeDir(dispatch: Dispatch,
                parent: Option[String],
                dir: String): FileListDirChangeAction = {
    
    val future = readDir(parent, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(dir, currDir))
    }

    FileListDirChangeAction(FutureTask("Changing Dir", future))
  }

  def updateDir(dispatch: Dispatch, path: String): FileListDirUpdateAction = {
    val future = api.readDir(path).andThen {
      case Success(currDir) => dispatch(FileListDirUpdatedAction(currDir))
    }

    FileListDirUpdateAction(FutureTask("Updating Dir", future))
  }

  def createDir(dispatch: Dispatch,
                parent: String,
                dir: String,
                multiple: Boolean): FileListDirCreateAction = {

    val names =
      if (multiple) dir.split(nodePath.sep.head).toList
      else List(dir)
    
    val future = for {
      _ <- mkDirs(parent :: names)
      currDir <- api.readDir(parent)
    } yield {
      dispatch(FileListDirCreatedAction(names.head, currDir))
      ()
    }

    FileListDirCreateAction(FutureTask("Creating Dir", future))
  }

  def mkDirs(dirs: List[String]): Future[Unit] = api.mkDirs(dirs)

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = api.readDir(parent, dir)

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = api.delete(parent, items)

  def deleteAction(dispatch: Dispatch,
                   dir: String,
                   items: Seq[FileListItem]): FileListTaskAction = {
    
    val future = for {
      _ <- delete(dir, items)
      currDir <- api.readDir(dir)
    } yield {
      dispatch(FileListDirUpdatedAction(currDir))
      ()
    }

    FileListTaskAction(FutureTask("Deleting Items", future))
  }

  def scanDirs(parent: String,
               items: Seq[FileListItem],
               onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    items.foldLeft(Future.successful(true)) { case (resF, item) =>
      resF.flatMap {
        case true if item.isDir =>
          readDir(Some(parent), item.name).flatMap { ls =>
            if (onNextDir(ls.path, ls.items)) scanDirs(ls.path, ls.items, onNextDir)
            else Future.successful(false)
          }
        case res => Future.successful(res)
      }
    }
  }

  def copyFile(srcDirs: List[String],
               srcItem: FileListItem,
               dstDirs: List[String],
               dstName: String,
               onExists: FileListItem => Future[Option[Boolean]],
               onProgress: Double => Future[Boolean]): Future[Boolean] = {

    api.writeFile(dstDirs, dstName, onExists).flatMap {
      case None => onProgress(srcItem.size)
      case Some(target) =>
        api.readFile(srcDirs, srcItem, 0.0).flatMap { source =>
          val buff = new Uint8Array(copyBufferBytes)

          def loop(): Future[Boolean] = {
            source.readNextBytes(buff).flatMap { bytesRead =>
              if (bytesRead == 0) target.setAttributes(srcItem).map(_ => true)
              else {
                target.writeNextBytes(buff, bytesRead).flatMap { position =>
                  onProgress(position).flatMap {
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

  case class FileListTaskAction(task: FutureTask[_]) extends TaskAction

  case class FileListParamsChangedAction(offset: Int,
                                         index: Int,
                                         selectedNames: Set[String]) extends Action

  case class FileListDirChangeAction(task: FutureTask[FileListDir]) extends TaskAction
  case class FileListDirChangedAction(dir: String, currDir: FileListDir) extends Action
  
  case class FileListDirUpdateAction(task: FutureTask[FileListDir]) extends TaskAction
  case class FileListDirUpdatedAction(currDir: FileListDir) extends Action
  
  case class FileListDirCreateAction(task: FutureTask[Unit]) extends TaskAction
  case class FileListDirCreatedAction(dir: String, currDir: FileListDir) extends Action
  
  case class FileListItemsViewedAction(sizes: Map[String, Double]) extends Action
}
