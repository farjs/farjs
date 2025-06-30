package farjs

import farjs.filelist.{FileListData, FileListPlugin}
import farjs.filelist.theme.FileListTheme
import scommons.react.ReactClass
import scommons.react.blessed.BlessedStyle

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object viewer {

  @js.native
  @JSImport("../viewer/ViewerContent.mjs", JSImport.Default)
  object ViewerContent extends ReactClass {
    
    def contentStyle(theme: FileListTheme): BlessedStyle = js.native
  }

  @js.native
  @JSImport("../viewer/ViewerController.mjs", JSImport.Default)
  object ViewerController extends ReactClass {

    var _createFileReader: js.Function0[ViewerFileReader] = js.native
    def scrollStyle(theme: FileListTheme): BlessedStyle = js.native
  }

  @js.native
  @JSImport("../viewer/ViewerHeader.mjs", JSImport.Default)
  object ViewerHeader extends ReactClass

  @js.native
  @JSImport("../viewer/ViewerInput.mjs", JSImport.Default)
  object ViewerInput extends ReactClass

  @js.native
  @JSImport("../viewer/ViewerPlugin.mjs", JSImport.Default)
  object ViewerPlugin extends FileListPlugin(js.native)

  @js.native
  @JSImport("../viewer/ViewerPluginUi.mjs", JSImport.Default)
  object ViewerPluginUi extends js.Function2[String, Double, ReactClass] {

    def apply(filePath: String, size: Double): ReactClass = js.native
  }

  @js.native
  @JSImport("../viewer/ViewerSearch.mjs", JSImport.Default)
  object ViewerSearch extends ReactClass

  @js.native
  @JSImport("../viewer/ViewItemsPopup.mjs", JSImport.Default)
  object ViewItemsPopup extends js.Function1[FileListData, ReactClass] {

    def apply(data: FileListData): ReactClass = js.native
  }
}
