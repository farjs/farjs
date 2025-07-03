package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.archiver.ArchiverPlugin
import farjs.copymove.CopyMovePlugin
import farjs.filelist._
import farjs.filelist.history.HistoryProvider
import farjs.fs.{FSPlugin, FSServices}
import farjs.ui.Dispatch
import scommons.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

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

  @js.native
  @JSImport("../viewer/quickview/QuickViewPlugin.mjs", JSImport.Default)
  object QuickViewPlugin extends FileListPlugin(js.native)

  @js.native
  @JSImport("../viewer/ViewerPlugin.mjs", JSImport.Default)
  object ViewerPlugin extends FileListPlugin(js.native)

  @js.native
  @JSImport("../file/FilePlugin.mjs", JSImport.Default)
  object FilePlugin extends FileListPlugin(js.native)

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
