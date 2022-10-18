package farjs.app.filelist.history

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryDao, HistoryFolderDao}

class HistoryFolderDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): HistoryDao =
    new HistoryFolderDao(ctx, maxItemsCount)
}
