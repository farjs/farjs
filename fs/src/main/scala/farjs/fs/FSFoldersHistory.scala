package farjs.fs

import farjs.filelist.history._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FSFoldersHistoryProps(currDirPath: String)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  val foldersHistoryKind: HistoryKind = HistoryKind("farjs.folders", 100)

  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider
    val props = compProps.wrapped
    val currDirPath = props.currDirPath

    useLayoutEffect({ () =>
      if (currDirPath.nonEmpty) {
        for {
          foldersHistory <- historyProvider.get(foldersHistoryKind).toFuture
          _ <- foldersHistory.save(History(currDirPath, js.undefined)).toFuture
        } yield ()
      }
      ()
    }, List(currDirPath))

    null
  }
}
