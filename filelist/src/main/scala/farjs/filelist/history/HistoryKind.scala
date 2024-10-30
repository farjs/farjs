package farjs.filelist.history

import scala.scalajs.js

trait HistoryKind extends js.Object {

  val name: String
  val maxItemsCount: Int
}

object HistoryKind {

  def apply(name: String, maxItemsCount: Int): HistoryKind = {
    js.Dynamic.literal(
      name = name,
      maxItemsCount = maxItemsCount
    ).asInstanceOf[HistoryKind]
  }

  def unapply(arg: HistoryKind): Option[(String, Int)] = {
    Some((
      arg.name,
      arg.maxItemsCount
    ))
  }
}
