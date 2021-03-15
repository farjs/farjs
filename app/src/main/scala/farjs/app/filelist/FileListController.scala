package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListController(actions: FileListActions)
  extends BaseStateController[FarjsStateDef, FileListBrowserProps] {

  lazy val uiComponent: UiComponent[FileListBrowserProps] = FileListBrowser
  
  private lazy val plugins: Seq[FileListPlugin] = Nil

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListBrowserProps = {
    FileListBrowserProps(
      dispatch = dispatch,
      actions = actions,
      data = state.fileListsState,
      plugins = plugins
    )
  }
}
