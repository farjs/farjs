package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.archiver.ArchiverPlugin
import farjs.copymove.CopyMovePlugin
import farjs.filelist._
import farjs.fs.{FSPlugin, FSServices}
import farjs.ui.Dispatch
import farjs.viewer.ViewerPlugin
import farjs.viewer.quickview.QuickViewPlugin
import scommons.react._

class FileListRoot(dispatch: Dispatch, module: FileListModule) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <(FileListServices.Context.Provider)(^.contextValue := module.fileListServices)(
      <(FSServices.Context.Provider)(^.contextValue := module.fsServices)(
        <(fileListComp)(^.wrapped := FileListBrowserProps(
          dispatch = dispatch,
          plugins = plugins
        ))()
      )
    )
  }
}

object FileListRoot {
  
  private[filelist] var fileListComp: ReactClass = FileListBrowser()

  private lazy val plugins: Seq[FileListPlugin] = List(
    QuickViewPlugin,
    ArchiverPlugin,
    ViewerPlugin,
    CopyMovePlugin,
    FSPlugin,
    FileListUiPlugin
  )
}
