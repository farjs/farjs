package farjs.domain.dao

import farjs.domain._

class HistoryCopyItemDao(c: FarjsDBContext, maxItemsCount: Int = 50)
  extends HistoryDao(c, tableName = "history_copy_items", maxItemsCount)
