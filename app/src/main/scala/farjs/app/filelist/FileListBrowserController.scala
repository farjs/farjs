package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.archiver.ArchiverPlugin
import farjs.copymove.CopyMovePlugin
import farjs.filelist._
import farjs.viewer.ViewerPlugin
import farjs.viewer.quickview.QuickViewPlugin
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

object FileListBrowserController extends BaseStateController[FarjsStateDef, FileListBrowserProps] {

  lazy val uiComponent: UiComponent[FileListBrowserProps] = FileListBrowser
  
  private lazy val plugins: Seq[FileListPlugin] = List(
    QuickViewPlugin,
    ArchiverPlugin,
    ViewerPlugin,
    CopyMovePlugin
  )

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListBrowserProps = {
    FileListBrowserProps(
      dispatch = dispatch,
      plugins = plugins
    )
  }
}
