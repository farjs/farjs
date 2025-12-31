package farjs.app.filelist

import farjs.filelist._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../app/filelist/FileListBrowser.mjs", JSImport.Default)
object FileListBrowser extends js.Function1[FileListPluginHandler, ReactClass] {

  def apply(pluginHandler: FileListPluginHandler): ReactClass = js.native
}

trait FileListPluginHandler extends js.Object {

  def openCurrItem(dispatch: Dispatch, stack: PanelStack): Unit

  def openPluginUi(dispatch: Dispatch, key: KeyboardKey, stacks: WithStacksProps): js.Promise[js.UndefOr[ReactClass]]
}

@js.native
@JSImport("../app/filelist/FileListPluginHandler.mjs", JSImport.Default)
object FileListPluginHandler extends js.Function1[js.Array[FileListPlugin], FileListPluginHandler] {

  def apply(plugins: js.Array[FileListPlugin]): FileListPluginHandler = js.native
}
