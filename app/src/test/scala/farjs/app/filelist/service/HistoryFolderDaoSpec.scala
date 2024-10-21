package farjs.app.filelist.service

import farjs.domain.FarjsDBContext
import farjs.domain.dao.{BaseHistoryDao, HistoryFolderDao}

class HistoryFolderDaoSpec extends BaseHistoryDaoSpec {

  protected def createDao(ctx: FarjsDBContext, maxItemsCount: Int): BaseHistoryDao =
    new HistoryFolderDao(ctx, maxItemsCount)
}
