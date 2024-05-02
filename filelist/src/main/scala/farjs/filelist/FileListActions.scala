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
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Success
import scala.util.control.NonFatal

class FileListActions(var api: FileListApi) extends js.Object {

  def changeDir(dispatch: Dispatch, path: String, dir: String): TaskAction = {
    val future = api.readDir(path, dir).toFuture.andThen {
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
      _ <- api.mkDirs(js.Array((parent :: names): _*)).toFuture
      currDir <- api.readDir(parent, js.undefined).toFuture
    } yield {
      dispatch(FileListItemCreatedAction(names.head, currDir))
      ()
    }

    TaskAction(Task("Creating Dir", future))
  }

  def deleteItems(dispatch: Dispatch,
                  parent: String,
                  items: js.Array[FileListItem]): TaskAction = {
    
    val future = api.delete(parent, items).toFuture.andThen {
      case Success(_) => dispatch(updateDir(dispatch, parent))
    }

    TaskAction(Task("Deleting Items", future))
  }

  def scanDirs(parent: String,
               items: js.Array[FileListItem],
               onNextDir: js.Function2[String, js.Array[FileListItem], Boolean]): js.Promise[Boolean] = {

    items.foldLeft(Future.successful(true)) { case (resF, item) =>
      resF.flatMap {
        case true if item.isDir =>
          api.readDir(parent, item.name).toFuture.flatMap { ls =>
            val dirItems = ls.items
            if (onNextDir(ls.path, dirItems)) scanDirs(ls.path, dirItems, onNextDir).toFuture
            else Future.successful(false)
          }
        case res => Future.successful(res)
      }
    }.toJSPromise
  }

  def copyFile(srcDir: String,
               srcItem: FileListItem,
               dstFileF: js.Promise[js.UndefOr[FileTarget]],
               onProgress: js.Function1[Double, js.Promise[Boolean]]): js.Promise[Boolean] = {

    dstFileF.toFuture.flatMap(_.toOption match {
      case None => onProgress(srcItem.size).toFuture
      case Some(target) =>
        api.readFile(srcDir, srcItem, 0.0).toFuture.flatMap { source =>
          val buff = new Uint8Array(copyBufferBytes)

          def loop(): Future[Boolean] = {
            source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
              if (bytesRead == 0) target.setAttributes(srcItem).toFuture.map(_ => true)
              else {
                target.writeNextBytes(buff, bytesRead).toFuture.flatMap { position =>
                  onProgress(position).toFuture.flatMap {
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
    }).toJSPromise
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
