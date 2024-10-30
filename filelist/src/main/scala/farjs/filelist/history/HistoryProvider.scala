package farjs.filelist.history

import scala.scalajs.js

trait HistoryProvider extends js.Object {

  def get(kind: HistoryKind): js.Promise[HistoryService]
}
