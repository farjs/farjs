package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{BaseHistoryDao, HistoryMkDirDao}

class HistoryMkDirDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): BaseHistoryDao =
    new HistoryMkDirDao(ctx, maxItemsCount)
}
