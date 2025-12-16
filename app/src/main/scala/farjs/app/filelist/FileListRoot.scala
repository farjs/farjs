package farjs.app.filelist

import farjs.app.filelist.FileListRoot._
import farjs.archiver.ArchiverPlugin
import farjs.filelist._
import farjs.filelist.history.HistoryProvider
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.ui.Dispatch
import scommons.react._
import scommons.react.raw.NativeContext

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
          <(fileListComp)(^.plain := FileListBrowserProps(
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
  private object QuickViewPlugin extends FileListPlugin(js.native)

  @js.native
  @JSImport("../viewer/ViewerPlugin.mjs", JSImport.Default)
  private object ViewerPlugin extends FileListPlugin(js.native)

  @js.native
  @JSImport("../file/FilePlugin.mjs", JSImport.Default)
  private object FilePlugin extends FileListPlugin(js.native)

  trait FolderShortcutsService extends js.Object {

    def getAll(): js.Promise[js.Array[js.UndefOr[String]]]

    def save(index: Int, path: String): js.Promise[Unit]

    def delete(index: Int): js.Promise[Unit]
  }

  trait FSServices extends js.Object {

    val folderShortcuts: FolderShortcutsService
  }

  @js.native
  @JSImport("../fs/FSServices.mjs", JSImport.Default)
  object FSServices extends js.Object {

    val Context: NativeContext = js.native

    def useServices(): FSServices = js.native
  }

  @js.native
  @JSImport("../fs/FSFileListActions.mjs", JSImport.Default)
  object FSFileListActions extends js.Object {

    val instance: FileListActions = js.native
  }

  @js.native
  @JSImport("../fs/FSPlugin.mjs", JSImport.Default)
  class FSPlugin(reducer: js.Function2[FileListState, js.Any, FileListState])
    extends FileListPlugin(js.native) {

    val component: ReactClass = js.native

    def init(parentDispatch: Dispatch, stack: PanelStack): Unit = js.native

    def initDispatch(parentDispatch: Dispatch,
                     reducer: js.Function2[FileListState, js.Any, FileListState],
                     stack: PanelStack,
                     item: PanelStackItem[FileListState]
                    ): PanelStackItem[FileListState] = js.native
  }

  @js.native
  @JSImport("../fs/FSPlugin.mjs", JSImport.Default)
  object FSPlugin extends js.Object {

    val instance: FSPlugin = js.native
  }

  @js.native
  @JSImport("../copymove/CopyMovePlugin.mjs", JSImport.Default)
  object CopyMovePlugin extends js.Object {

    val instance: FileListPlugin = js.native
  }

  private lazy val plugins: js.Array[FileListPlugin] = js.Array(
    QuickViewPlugin,
    ArchiverPlugin,
    ViewerPlugin,
    CopyMovePlugin.instance,
    FSPlugin.instance,
    FileListUiPlugin,
    FilePlugin
  )
}
