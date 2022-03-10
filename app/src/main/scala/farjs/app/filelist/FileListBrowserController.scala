package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.app.filelist.zip.ZipPlugin
import farjs.filelist._
import farjs.filelist.quickview.QuickViewPlugin
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

object FileListBrowserController extends BaseStateController[FarjsStateDef, FileListBrowserProps] {

  lazy val uiComponent: UiComponent[FileListBrowserProps] = FileListBrowser
  
  private lazy val plugins: Seq[FileListPlugin] = List(
    QuickViewPlugin,
    ZipPlugin
  )

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListBrowserProps = {
    FileListBrowserProps(
      dispatch = dispatch,
      plugins = plugins
    )
  }
}
