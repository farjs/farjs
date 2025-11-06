package farjs

import farjs.filelist.history.HistoryKind
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object copymove {

  @js.native
  @JSImport("../copymove/CopyItemsPopup.mjs", JSImport.Default)
  object CopyItemsPopup extends ReactClass {

    val copyItemsHistoryKind: HistoryKind = js.native
  }

  @js.native
  @JSImport("../copymove/CopyItemsStats.mjs", JSImport.Default)
  object CopyItemsStats extends ReactClass

  @js.native
  @JSImport("../copymove/CopyProcess.mjs", JSImport.Default)
  object CopyProcess extends ReactClass

  @js.native
  @JSImport("../copymove/CopyProgressPopup.mjs", JSImport.Default)
  object CopyProgressPopup extends ReactClass

  @js.native
  @JSImport("../copymove/FileExistsPopup.mjs", JSImport.Default)
  object FileExistsPopup extends ReactClass

  @js.native
  @JSImport("../copymove/MoveProcess.mjs", JSImport.Default)
  object MoveProcess extends ReactClass

  @js.native
  @JSImport("../copymove/CopyMoveUi.mjs", JSImport.Default)
  object CopyMoveUi extends js.Function1[CopyMoveUiOptions, ReactClass] {

    override def apply(options: CopyMoveUiOptions): ReactClass = js.native
  }
}
