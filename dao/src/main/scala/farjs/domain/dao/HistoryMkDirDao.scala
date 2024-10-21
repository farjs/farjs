package farjs.domain.dao

import farjs.domain._

class HistoryMkDirDao(c: FarjsDBContext, maxItemsCount: Int = 50)
  extends BaseHistoryDao(c, tableName = "history_mkdirs", maxItemsCount)
