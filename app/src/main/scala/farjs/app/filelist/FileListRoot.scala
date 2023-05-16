package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.archiver.ArchiverPlugin
import farjs.copymove.CopyMovePlugin
import farjs.file.FileServices
import farjs.filelist._
import farjs.fs.{FSPlugin, FSServices}
import farjs.ui.Dispatch
import farjs.viewer.ViewerPlugin
import farjs.viewer.quickview.QuickViewPlugin
import scommons.react._

class FileListRoot(dispatch: Dispatch,
                   module: FileListModule,
                   withPortalsComp: UiComponent[Unit]
                  ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <(FileListServices.Context.Provider)(^.contextValue := module.fileListServices)(
      <(FSServices.Context.Provider)(^.contextValue := module.fsServices)(
        <(FileServices.Context.Provider)(^.contextValue := module.fileServices)(
          <(withPortalsComp())()(
            <(fileListComp)(^.wrapped := FileListBrowserProps(
              dispatch = dispatch,
              plugins = plugins
            ))(),

            compProps.children
          )
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
    FileListUiPlugin
  )
}
