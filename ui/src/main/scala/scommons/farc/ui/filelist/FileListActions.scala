package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.redux.Action
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.react.redux.task.{FutureTask, TaskAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JavaScriptException
import scala.util.{Failure, Success}

trait FileListActions {

  protected def api: FileListApi

  def changeDir(dispatch: Dispatch, isRight: Boolean, parent: Option[String], dir: String): FileListDirChangeAction = {
    val future = api.readDir(parent, dir).andThen {
      case Success(currDir) => dispatch(FileListDirChangedAction(isRight, dir, currDir))
      case Failure(e) => onError(())(e)
    }

    FileListDirChangeAction(FutureTask("Changing Dir", future))
  }

  protected def onError[T](value: T): Throwable => T = {
    case JavaScriptException(error) =>
      println(s"$error")
      value
    case error =>
      println(s"$error")
      value
  }
}

object FileListActions {

  case class FileListParamsChangedAction(isRight: Boolean,
                                         isActive: Boolean,
                                         offset: Int,
                                         index: Int,
                                         selectedNames: Set[String]) extends Action

  case class FileListDirChangeAction(task: FutureTask[FileListDir]) extends TaskAction
  case class FileListDirChangedAction(isRight: Boolean, dir: String, currDir: FileListDir) extends Action
}
