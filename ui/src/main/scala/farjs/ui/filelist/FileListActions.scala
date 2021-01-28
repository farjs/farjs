package farjs.ui.filelist

import farjs.api.filelist._
import farjs.ui.filelist.FileListActions._
import io.github.shogowada.scalajs.reactjs.redux.Action
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs.Process.Platform
import scommons.nodejs._
import scommons.react.redux.task.{FutureTask, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.Success

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
}

object FileListActions {

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
}
