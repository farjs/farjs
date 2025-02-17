package farjs.file

import scommons.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait FilePluginUiParams extends js.Object {
  val showFileViewHistoryPopup: Boolean
}

object FilePluginUiParams {

  def apply(showFileViewHistoryPopup: Boolean = false): FilePluginUiParams = {
    js.Dynamic.literal(
      showFileViewHistoryPopup = showFileViewHistoryPopup
    ).asInstanceOf[FilePluginUiParams]
  }

  def unapply(arg: FilePluginUiParams): Option[Boolean] = {
    Some(
      arg.showFileViewHistoryPopup
    )
  }
}

  @js.native
@JSImport("../file/FilePluginUi.mjs", JSImport.Default)
object FilePluginUi extends js.Function1[FilePluginUiParams, ReactClass] {

  def apply(params: FilePluginUiParams): ReactClass = js.native
}
