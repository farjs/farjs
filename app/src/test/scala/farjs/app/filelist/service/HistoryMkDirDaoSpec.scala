package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryDao, HistoryMkDirDao}

class HistoryMkDirDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): HistoryDao =
    new HistoryMkDirDao(ctx, maxItemsCount)
}
