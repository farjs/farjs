package farjs.app.filelist.fs

import farjs.filelist.FileListServices
import scommons.react._
import scommons.react.hooks._

case class FSFoldersHistoryProps(currDirPath: String)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val props = compProps.wrapped
    val currDirPath = props.currDirPath

    useLayoutEffect({ () =>
      if (currDirPath.nonEmpty) {
        services.foldersHistory.save(currDirPath)
      }
      ()
    }, List(currDirPath))

    null
  }
}
