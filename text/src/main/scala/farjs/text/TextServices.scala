package farjs.text

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class TextServices(fileViewHistory: FileViewHistoryService)

object TextServices {

  val Context: ReactContext[TextServices] = ReactContext[TextServices](defaultValue = null)

  def useServices: TextServices = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "TextServices.Context is not found." +
          "\nPlease, make sure you use TextServices.Context.Provider in parent components"
      ))
    }
    ctx
  }
}
