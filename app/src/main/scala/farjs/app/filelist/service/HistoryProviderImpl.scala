package farjs.app.filelist.service

import farjs.filelist.history._
import farjs.filelist.popups.MakeFolderController.mkDirsHistoryKind

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

class HistoryProviderImpl(mkDirService: FileListHistoryService) extends HistoryProvider {

  private val mkDirsHistory = new HistoryServiceImpl(mkDirService)
  private val noopHistory = new NoopHistoryService
  
  override def get(kind: HistoryKind): js.Promise[HistoryService] = {
    val historyService =
      if (kind.name == mkDirsHistoryKind.name) mkDirsHistory
      else noopHistory
    
    js.Promise.resolve[HistoryService](historyService)
  }
}

class HistoryServiceImpl(oldService: FileListHistoryService) extends NoopHistoryService {

  override def getAll: js.Promise[js.Array[History]] = oldService.getAll.map { items =>
    js.Array[History](items.map(i => History(i, js.undefined)): _*)
  }.toJSPromise

  override def save(h: History): js.Promise[Unit] =
    oldService.save(h.item).toJSPromise
}

class NoopHistoryService extends HistoryService {

  override def getAll: js.Promise[js.Array[History]] =
    js.Promise.resolve[js.Array[History]](js.Array[History]())

  override def getOne(item: String): js.Promise[js.UndefOr[History]] =
    js.Promise.resolve[js.UndefOr[History]](js.undefined)

  override def save(h: History): js.Promise[Unit] =
    js.Promise.resolve[Unit](())
}
