package farjs.domain

import scala.scalajs.js

case class HistoryEntity(item: String,
                         params: Option[js.Object],
                         updatedAt: Long)
