package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryCopyItemDao, BaseHistoryDao}

class HistoryCopyItemDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): BaseHistoryDao =
    new HistoryCopyItemDao(ctx, maxItemsCount)
}
