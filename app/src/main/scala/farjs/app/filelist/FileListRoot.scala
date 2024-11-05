package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.archiver.ArchiverPlugin
import farjs.copymove.CopyMovePlugin
import farjs.file.FilePlugin
import farjs.filelist._
import farjs.filelist.history.HistoryProvider
import farjs.fs.{FSPlugin, FSServices}
import farjs.ui.Dispatch
import farjs.viewer.ViewerPlugin
import farjs.viewer.quickview.QuickViewPlugin
import scommons.react._

class FileListRoot(dispatch: Dispatch,
                   module: FileListModule,
                   withPortalsComp: ReactClass
                  ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <(HistoryProvider.Context.Provider)(^.contextValue := module.historyProvider)(
      <(FSServices.Context.Provider)(^.contextValue := module.fsServices)(
        <(withPortalsComp)()(
          <(fileListComp)(^.wrapped := FileListBrowserProps(
            dispatch = dispatch,
            plugins = plugins
          ))(),

          compProps.children
        )
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
    FileListUiPlugin,
    FilePlugin
  )
}
