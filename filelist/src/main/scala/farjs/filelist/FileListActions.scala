package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api._
import farjs.filelist.sort.SortMode
import farjs.ui.Dispatch
import farjs.ui.task.{Task, TaskAction}
import scommons.nodejs.{path => nodePath}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Success
import scala.util.control.NonFatal

trait FileListActions {

  protected def api: FileListApi

  def isLocalFS: Boolean

  def getDriveRoot(path: String): Future[Option[String]]

  def capabilities: Set[String] = api.capabilities

  def changeDir(dispatch: Dispatch,
                parent: Option[String],
                dir: String): FileListDirChangeAction = {
    
    val future = readDir(parent, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(dir, currDir))
    }

    FileListDirChangeAction(Task("Changing Dir", future))
  }

  def updateDir(dispatch: Dispatch, path: String): FileListDirUpdateAction = {
    val future = api.readDir(path).andThen {
      case Success(currDir) => dispatch(FileListDirUpdatedAction(currDir))
    }

    FileListDirUpdateAction(Task("Updating Dir", future))
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
      dispatch(FileListItemCreatedAction(names.head, currDir))
      ()
    }

    FileListDirCreateAction(Task("Creating Dir", future))
  }

  def mkDirs(dirs: List[String]): Future[Unit] = api.mkDirs(dirs)

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = api.readDir(parent, dir)

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = api.delete(parent, items)

  def deleteAction(dispatch: Dispatch,
                   dir: String,
                   items: Seq[FileListItem]): FileListTaskAction = {
    
    val future = delete(dir, items).andThen {
      case Success(_) => dispatch(updateDir(dispatch, dir))
    }

    FileListTaskAction(Task("Deleting Items", future))
  }

  def scanDirs(parent: String,
               items: Seq[FileListItem],
               onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    items.foldLeft(Future.successful(true)) { case (resF, item) =>
      resF.flatMap {
        case true if item.isDir =>
          readDir(Some(parent), item.name).flatMap { ls =>
            val dirItems = ls.items.toSeq
            if (onNextDir(ls.path, dirItems)) scanDirs(ls.path, dirItems, onNextDir)
            else Future.successful(false)
          }
        case res => Future.successful(res)
      }
    }
  }

  def writeFile(parentDirs: List[String],
                fileName: String,
                onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = {

    api.writeFile(parentDirs, fileName, onExists)
  }

  def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] = {
    api.readFile(parentDirs, file, position)
  }

  def copyFile(srcDirs: List[String],
               srcItem: FileListItem,
               dstFileF: Future[Option[FileTarget]],
               onProgress: Double => Future[Boolean]): Future[Boolean] = {

    dstFileF.flatMap {
      case None => onProgress(srcItem.size)
      case Some(target) =>
        readFile(srcDirs, srcItem, 0.0).flatMap { source =>
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
        }.transformWith { tryRes =>
          val res = tryRes.getOrElse(false)
          if (!res) target.delete().flatMap(_ => Future.fromTry(tryRes))
          else Future.fromTry(tryRes)
        }
    }
  }
}

object FileListActions {
  
  private val copyBufferBytes: Int = 64 * 1024

  sealed trait FileListTaskAction extends TaskAction
  object FileListTaskAction {
    def apply(task: Task): FileListTaskAction =
      js.Dynamic.literal(task = task).asInstanceOf[FileListTaskAction]

    def unapply(arg: FileListTaskAction): Option[Task] = Some(arg.task)
  }

  sealed trait FileListParamsChangedAction extends js.Object {
    val action: String
    val offset: Int
    val index: Int
    val selectedNames: js.Set[String]
  }
  object FileListParamsChangedAction {
    val name = "FileListParamsChangedAction"

    def apply(offset: Int,
              index: Int,
              selectedNames: js.Set[String]): FileListParamsChangedAction = {
      js.Dynamic.literal(
        action = name,
        offset = offset,
        index = index,
        selectedNames = selectedNames
      ).asInstanceOf[FileListParamsChangedAction]
    }

    def unapply(arg: FileListParamsChangedAction): Option[(Int, Int, js.Set[String])] =
      Some((
        arg.offset,
        arg.index,
        arg.selectedNames
      ))
  }

  sealed trait FileListDirChangeAction extends TaskAction
  object FileListDirChangeAction {
    def apply(task: Task): FileListDirChangeAction =
      js.Dynamic.literal(task = task).asInstanceOf[FileListDirChangeAction]

    def unapply(arg: FileListDirChangeAction): Option[Task] = Some(arg.task)
  }

  sealed trait FileListDirChangedAction extends js.Object {
    val action: String
    val dir: String
    val currDir: FileListDir
  }
  object FileListDirChangedAction {
    val name = "FileListDirChangedAction"

    def apply(dir: String,
              currDir: FileListDir): FileListDirChangedAction = {
      js.Dynamic.literal(
        action = name,
        dir = dir,
        currDir = currDir
      ).asInstanceOf[FileListDirChangedAction]
    }

    def unapply(arg: FileListDirChangedAction): Option[(String, FileListDir)] =
      Some((
        arg.dir,
        arg.currDir
      ))
  }
  
  sealed trait FileListDirUpdateAction extends TaskAction
  object FileListDirUpdateAction {
    def apply(task: Task): FileListDirUpdateAction =
      js.Dynamic.literal(task = task).asInstanceOf[FileListDirUpdateAction]

    def unapply(arg: FileListDirUpdateAction): Option[Task] = Some(arg.task)
  }

  case class FileListDirUpdatedAction(currDir: FileListDir)

  sealed trait FileListDirCreateAction extends TaskAction
  object FileListDirCreateAction {
    def apply(task: Task): FileListDirCreateAction =
      js.Dynamic.literal(task = task).asInstanceOf[FileListDirCreateAction]

    def unapply(arg: FileListDirCreateAction): Option[Task] = Some(arg.task)
  }

  case class FileListItemCreatedAction(name: String, currDir: FileListDir)
  
  case class FileListDiskSpaceUpdatedAction(diskSpace: Double)
  case class FileListSortAction(mode: SortMode)
}
