package farjs.app.filelist.service

import farjs.copymove.CopyMoveUi.copyItemsHistoryKind
import farjs.file.FileViewHistory
import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.filelist.history._
import farjs.filelist.popups.MakeFolderController.mkDirsHistoryKind
import farjs.filelist.popups.SelectController.selectPatternsHistoryKind
import farjs.fs.FSFoldersHistory.foldersHistoryKind

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

class HistoryProviderImpl(mkDirService: FileListHistoryService,
                          folderService: FileListHistoryService,
                          selectPatternService: FileListHistoryService,
                          copyItemService: FileListHistoryService,
                          fileViewHistoryService: FileViewHistoryServiceImpl
                         ) extends HistoryProvider {

  private val mkDirsHistory = new OldHistoryServiceImpl(mkDirService)
  private val foldersHistory = new OldHistoryServiceImpl(folderService)
  private val selectPatternsHistory = new OldHistoryServiceImpl(selectPatternService)
  private val copyItemsHistory = new OldHistoryServiceImpl(copyItemService)
  private val fileViewsHistory = new OldViewHistoryServiceImpl(fileViewHistoryService)
  private val noopHistory = new NoopHistoryService
  
  override def get(kind: HistoryKind): js.Promise[HistoryService] = {
    val historyService = kind.name match {
      case mkDirsHistoryKind.name => mkDirsHistory
      case foldersHistoryKind.name => foldersHistory
      case selectPatternsHistoryKind.name => selectPatternsHistory
      case copyItemsHistoryKind.name => copyItemsHistory
      case fileViewsHistoryKind.name => fileViewsHistory
      case _ => noopHistory
    }
    js.Promise.resolve[HistoryService](historyService)
  }
}

class OldViewHistoryServiceImpl(oldService: FileViewHistoryServiceImpl) extends NoopHistoryService {

  override def getAll: js.Promise[js.Array[History]] = oldService.getAll.map { items =>
    js.Array[History](items.map(FileViewHistory.toHistory): _*)
  }.toJSPromise

  override def getOne(item: String): js.Promise[js.UndefOr[History]] =
    oldService.getOne(item, isEdit = false).map {
      case Some(h) => FileViewHistory.toHistory(h): js.UndefOr[History]
      case None => js.undefined: js.UndefOr[History]
    }.toJSPromise

  override def save(h: History): js.Promise[Unit] =
    oldService.save(FileViewHistory.fromHistory(h).orNull).toJSPromise
}

class OldHistoryServiceImpl(oldService: FileListHistoryService) extends NoopHistoryService {

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
