package farjs.fs

import farjs.fs.popups.FolderShortcutsService
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class FSServices(folderShortcuts: FolderShortcutsService)

object FSServices {

  val Context: ReactContext[FSServices] = ReactContext[FSServices](defaultValue = null)

  def useServices: FSServices = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "FSServices.Context is not found." +
          "\nPlease, make sure you use FSServices.Context.Provider in parent components"
      ))
    }
    ctx
  }
}
