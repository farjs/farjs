package farjs.file

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class FileServices(fileViewHistory: FileViewHistoryService)

object FileServices {

  val Context: ReactContext[FileServices] = ReactContext[FileServices](defaultValue = null)

  def useServices: FileServices = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "FileServices.Context is not found." +
          "\nPlease, make sure you use FileServices.Context.Provider in parent components"
      ))
    }
    ctx
  }
}
