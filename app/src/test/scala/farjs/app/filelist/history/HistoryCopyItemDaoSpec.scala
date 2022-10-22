package farjs.app.filelist.history

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryCopyItemDao, HistoryDao}

class HistoryCopyItemDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): HistoryDao =
    new HistoryCopyItemDao(ctx, maxItemsCount)
}
