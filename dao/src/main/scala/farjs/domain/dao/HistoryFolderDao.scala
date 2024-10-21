package farjs.domain.dao

import farjs.domain._

class HistoryFolderDao(c: FarjsDBContext, maxItemsCount: Int = 100)
  extends BaseHistoryDao(c, tableName = "history_folders", maxItemsCount)
