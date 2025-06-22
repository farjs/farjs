package farjs.viewer

import farjs.filelist.FileListPlugin
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object quickview {

  @js.native
  @JSImport("../viewer/quickview/QuickViewDir.mjs", JSImport.Default)
  object QuickViewDir extends ReactClass

  @js.native
  @JSImport("../viewer/quickview/QuickViewFile.mjs", JSImport.Default)
  object QuickViewFile extends ReactClass

  @js.native
  @JSImport("../viewer/quickview/QuickViewPanel.mjs", JSImport.Default)
  object QuickViewPanel extends ReactClass

  @js.native
  @JSImport("../viewer/quickview/QuickViewPlugin.mjs", JSImport.Default)
  object QuickViewPlugin extends FileListPlugin(js.native)
}
