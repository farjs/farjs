package farjs.filelist

import farjs.filelist.history.{FileListHistoryService, HistoryProvider}
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListServices(historyProvider: HistoryProvider,
                            foldersHistory: FileListHistoryService,
                            mkDirsHistory: FileListHistoryService,
                            selectPatternsHistory: FileListHistoryService,
                            copyItemsHistory: FileListHistoryService)

object FileListServices {

  val Context: ReactContext[FileListServices] = ReactContext[FileListServices](defaultValue = null)

  def useServices: FileListServices = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "FileListServices.Context is not found." +
          "\nPlease, make sure you use FileListServices.Context.Provider in parent components"
      ))
    }
    ctx
  }
}
