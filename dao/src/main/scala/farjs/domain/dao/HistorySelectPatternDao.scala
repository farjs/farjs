package farjs.domain.dao

import farjs.domain._

class HistorySelectPatternDao(c: FarjsDBContext, maxItemsCount: Int = 50)
  extends HistoryDao(c, tableName = "history_select_patterns", maxItemsCount)
