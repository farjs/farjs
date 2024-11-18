package farjs.filelist.history

import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait HistoryProvider extends js.Object {

  def get(kind: HistoryKind): js.Promise[HistoryService]
}

@js.native
@JSImport("@farjs/filelist/history/HistoryProvider.mjs", JSImport.Default)
object HistoryProvider extends js.Object {

  val Context: NativeContext = js.native

  def useHistoryProvider(): HistoryProvider = js.native
}
