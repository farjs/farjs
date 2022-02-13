package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist._
import farjs.filelist.fs.FSController
import farjs.filelist.quickview.QuickViewPlugin
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.{ReactClass, UiComponent}
import scommons.react.redux._

class FileListController(actions: FileListActions, fileListPopups: ReactClass)
  extends BaseStateController[FarjsStateDef, FileListBrowserProps] {

  lazy val uiComponent: UiComponent[FileListBrowserProps] =
    new FileListBrowser(new FSController(actions).apply(), fileListPopups)
  
  private lazy val plugins: Seq[FileListPlugin] = List(
    QuickViewPlugin
  )

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListBrowserProps = {
    FileListBrowserProps(
      dispatch = dispatch,
      data = state.fileListsState,
      plugins = plugins
    )
  }
}
