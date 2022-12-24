package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryDao, HistorySelectPatternDao}

class HistorySelectPatternDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): HistoryDao =
    new HistorySelectPatternDao(ctx, maxItemsCount)
}
