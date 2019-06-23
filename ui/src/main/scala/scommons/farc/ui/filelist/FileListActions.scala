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

  def changeDir(dispatch: Dispatch, isRight: Boolean, dir: Option[String]): FileListDirChangeAction = {
    val future = {
      for {
        currDir <- dir match {
          case None => api.currDir
          case Some(d) => api.changeDir(d)
        }
        files <- api.listFiles.recover {
          case e => onError(Nil)(e)
        }
      } yield (currDir, files)
    }.andThen {
      case Success((currDir, files)) => dispatch(FileListDirChangedAction(isRight, dir, currDir, files))
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
                                         offset: Int,
                                         index: Int,
                                         selectedNames: Set[String]) extends Action

  case class FileListDirChangeAction(task: FutureTask[(FileListDir, Seq[FileListItem])]) extends TaskAction
  case class FileListDirChangedAction(isRight: Boolean,
                                      dir: Option[String],
                                      currDir: FileListDir,
                                      files: Seq[FileListItem]) extends Action
}
