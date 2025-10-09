package farjs

import farjs.filelist.FileListActions
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object fs {

  @js.native
  @JSImport("../fs/FSFileListActions.mjs", JSImport.Default)
  object FSFileListActions extends js.Object {
    
    val instance: FileListActions = js.native
  }

  @js.native
  @JSImport("../fs/FSPanel.mjs", JSImport.Default)
  object FSPanel extends ReactClass

  @js.native
  @JSImport("../fs/FSPluginUi.mjs", JSImport.Default)
  object FSPluginUi extends js.Function1[FSPluginUiOptions, ReactClass] {

    override def apply(options: FSPluginUiOptions): ReactClass = js.native
  }
}
