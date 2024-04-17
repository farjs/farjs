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

  def capabilities: js.Set[FileListCapability] = api.capabilities

  def changeDir(dispatch: Dispatch, path: String, dir: String): TaskAction = {
    val future = readDir(path, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(dir, currDir))
    }

    TaskAction(Task("Changing Dir", future))
  }

  def updateDir(dispatch: Dispatch, path: String): TaskAction = {
    val future = api.readDir(path, js.undefined).toFuture.andThen {
      case Success(currDir) => dispatch(FileListDirUpdatedAction(currDir))
    }

    TaskAction(Task("Updating Dir", future))
  }

  def createDir(dispatch: Dispatch,
                parent: String,
                dir: String,
                multiple: Boolean): TaskAction = {

    val names =
      if (multiple) dir.split(nodePath.sep.head).toList
      else List(dir)
    
    val future = for {
      _ <- mkDirs(parent :: names)
      currDir <- api.readDir(parent, js.undefined).toFuture
    } yield {
      dispatch(FileListItemCreatedAction(names.head, currDir))
      ()
    }

    TaskAction(Task("Creating Dir", future))
  }

  def mkDirs(dirs: List[String]): Future[String] = api.mkDirs(js.Array(dirs: _*)).toFuture

  def readDir(path: String, dir: js.UndefOr[String]): Future[FileListDir] =
    api.readDir(path, dir).toFuture

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] =
    api.delete(parent, js.Array(items: _*)).toFuture

  def deleteAction(dispatch: Dispatch,
                   dir: String,
                   items: Seq[FileListItem]): TaskAction = {
    
    val future = delete(dir, items).andThen {
      case Success(_) => dispatch(updateDir(dispatch, dir))
    }

    TaskAction(Task("Deleting Items", future))
  }

  def scanDirs(parent: String,
               items: Seq[FileListItem],
               onNextDir: (String, Seq[FileListItem]) => Boolean): Future[Boolean] = {

    items.foldLeft(Future.successful(true)) { case (resF, item) =>
      resF.flatMap {
        case true if item.isDir =>
          readDir(parent, item.name).flatMap { ls =>
            val dirItems = ls.items.toSeq
            if (onNextDir(ls.path, dirItems)) scanDirs(ls.path, dirItems, onNextDir)
            else Future.successful(false)
          }
        case res => Future.successful(res)
      }
    }
  }

  def writeFile(parent: String,
                fileName: String,
                onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]): Future[js.UndefOr[FileTarget]] = {

    api.writeFile(parent, fileName, onExists).toFuture
  }

  def readFile(parent: String, file: FileListItem, position: Double): Future[FileSource] = {
    api.readFile(parent, file, position).toFuture
  }

  def copyFile(srcDir: String,
               srcItem: FileListItem,
               dstFileF: Future[js.UndefOr[FileTarget]],
               onProgress: Double => Future[Boolean]): Future[Boolean] = {

    dstFileF.flatMap(_.toOption match {
      case None => onProgress(srcItem.size)
      case Some(target) =>
        readFile(srcDir, srcItem, 0.0).flatMap { source =>
          val buff = new Uint8Array(copyBufferBytes)

          def loop(): Future[Boolean] = {
            source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
              if (bytesRead == 0) target.setAttributes(srcItem).toFuture.map(_ => true)
              else {
                target.writeNextBytes(buff, bytesRead).toFuture.flatMap { position =>
                  onProgress(position).flatMap {
                    case true => loop()
                    case false => Future.successful(false)
                  }
                }
              }
            }
          }

          loop().transformWith { res =>
            source.close().toFuture.recover {
              case NonFatal(ex) => println(s"Failed to close srcFile: ${source.file}, error: $ex")
            }.flatMap(_ => Future.fromTry(res))
          }
        }.transformWith { res =>
          target.close().toFuture.recover {
            case NonFatal(ex) => println(s"Failed to close dstFile: ${target.file}, error: $ex")
          }.flatMap(_ => Future.fromTry(res))
        }.transformWith { tryRes =>
          val res = tryRes.getOrElse(false)
          if (!res) target.delete().toFuture.flatMap(_ => Future.fromTry(tryRes))
          else Future.fromTry(tryRes)
        }
    })
  }
}

object FileListActions {
  
  private val copyBufferBytes: Int = 64 * 1024

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
  
  sealed trait FileListDirUpdatedAction extends js.Object {
    val action: String
    val currDir: FileListDir
  }
  object FileListDirUpdatedAction {
    val name = "FileListDirUpdatedAction"

    def apply(currDir: FileListDir): FileListDirUpdatedAction = {
      js.Dynamic.literal(
        action = name,
        currDir = currDir
      ).asInstanceOf[FileListDirUpdatedAction]
    }

    def unapply(arg: FileListDirUpdatedAction): Option[FileListDir] =
      Some(
        arg.currDir
      )
  }

  sealed trait FileListItemCreatedAction extends js.Object {
    val action: String
    val name: String
    val currDir: FileListDir
  }
  object FileListItemCreatedAction {
    val name = "FileListItemCreatedAction"

    def apply(name: String,
              currDir: FileListDir): FileListItemCreatedAction = {
      js.Dynamic.literal(
        action = FileListItemCreatedAction.name,
        name = name,
        currDir = currDir
      ).asInstanceOf[FileListItemCreatedAction]
    }

    def unapply(arg: FileListItemCreatedAction): Option[(String, FileListDir)] =
      Some((
        arg.name,
        arg.currDir
      ))
  }
  
  sealed trait FileListDiskSpaceUpdatedAction extends js.Object {
    val action: String
    val diskSpace: Double
  }
  object FileListDiskSpaceUpdatedAction {
    val name = "FileListDiskSpaceUpdatedAction"

    def apply(diskSpace: Double): FileListDiskSpaceUpdatedAction = {
      js.Dynamic.literal(
        action = name,
        diskSpace = diskSpace
      ).asInstanceOf[FileListDiskSpaceUpdatedAction]
    }

    def unapply(arg: FileListDiskSpaceUpdatedAction): Option[Double] =
      Some(
        arg.diskSpace
      )
  }

  sealed trait FileListSortAction extends js.Object {
    val action: String
    val mode: SortMode
  }
  object FileListSortAction {
    val name = "FileListSortAction"

    def apply(mode: SortMode): FileListSortAction = {
      js.Dynamic.literal(
        action = name,
        mode = mode
      ).asInstanceOf[FileListSortAction]
    }

    def unapply(arg: FileListSortAction): Option[SortMode] =
      Some(
        arg.mode
      )
  }
}
