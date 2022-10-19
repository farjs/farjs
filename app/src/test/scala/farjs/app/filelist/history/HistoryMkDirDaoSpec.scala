package farjs.app.filelist.history

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{HistoryDao, HistoryMkDirDao}

class HistoryMkDirDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): HistoryDao =
    new HistoryMkDirDao(ctx, maxItemsCount)
}
