package farjs.domain

import scala.scalajs.js

case class HistoryEntity(kindId: Int,
                         item: String,
                         params: Option[js.Object],
                         updatedAt: Long)
