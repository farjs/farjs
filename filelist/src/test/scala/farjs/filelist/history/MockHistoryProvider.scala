package farjs.filelist.history

import scala.scalajs.js

//noinspection NotImplementedCode
class MockHistoryProvider(
  getMock: HistoryKind => js.Promise[HistoryService] = _ => ???,
) extends HistoryProvider {

  override def get(kind: HistoryKind): js.Promise[HistoryService] = getMock(kind)
}
