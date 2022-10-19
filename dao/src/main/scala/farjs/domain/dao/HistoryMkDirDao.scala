package farjs.domain.dao

import farjs.domain._

class HistoryMkDirDao(c: FarjsDBContext, maxItemsCount: Int = 50)
  extends HistoryDao(c, tableName = "history_mkdirs", maxItemsCount)
