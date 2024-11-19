package farjs.domain

import scala.scalajs.js

trait HistoryKindEntity extends js.Object {

  val id: Int
  val name: String
}

object HistoryKindEntity {

  def apply(id: Int, name: String): HistoryKindEntity = {
    js.Dynamic.literal(
      id = id,
      name = name
    ).asInstanceOf[HistoryKindEntity]
  }

  def unapply(arg: HistoryKindEntity): Option[(Int, String)] = {
    Some((
      arg.id,
      arg.name
    ))
  }

  def copy(p: HistoryKindEntity)(id: Int = p.id,
                                 name: String = p.name): HistoryKindEntity = {

    HistoryKindEntity(id, name)
  }
}
