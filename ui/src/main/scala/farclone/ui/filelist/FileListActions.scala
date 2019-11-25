package farclone.ui.filelist

import farclone.api.filelist._
import farclone.ui.filelist.FileListActions._
import io.github.shogowada.scalajs.reactjs.redux.Action
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.redux.task.{FutureTask, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

trait FileListActions {

  protected def api: FileListApi

  def changeDir(dispatch: Dispatch,
                isRight: Boolean,
                parent: Option[String],
                dir: String): FileListDirChangeAction = {
    
    val future = api.readDir(parent, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(isRight, dir, currDir))
    }

    FileListDirChangeAction(FutureTask("Changing Dir", future))
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

  case class FileListDirChangeAction(task: FutureTask[FileListDir]) extends TaskAction
  case class FileListDirChangedAction(isRight: Boolean, dir: String, currDir: FileListDir) extends Action
  
  case class FileListItemsDeleteAction(task: FutureTask[Unit]) extends TaskAction
  case class FileListItemsDeletedAction(isRight: Boolean) extends Action
}
