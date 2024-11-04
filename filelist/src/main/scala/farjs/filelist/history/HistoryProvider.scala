package farjs.filelist.history

import scommons.react.ReactContext
import scommons.react.hooks.useContext

import scala.scalajs.js

trait HistoryProvider extends js.Object {

  def get(kind: HistoryKind): js.Promise[HistoryService]
}

object HistoryProvider {

  val Context: ReactContext[HistoryProvider] = ReactContext[HistoryProvider](defaultValue = null)

  def useHistoryProvider: HistoryProvider = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "HistoryProvider.Context is not found." +
          "\nPlease, make sure you use HistoryProvider.Context.Provider in parent components"
      ))
    }
    ctx
  }
}
