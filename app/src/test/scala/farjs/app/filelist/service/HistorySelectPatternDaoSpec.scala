package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{BaseHistoryDao, HistorySelectPatternDao}

class HistorySelectPatternDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): BaseHistoryDao =
    new HistorySelectPatternDao(ctx, maxItemsCount)
}
