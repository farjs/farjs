package farjs.filelist.history

import scala.scalajs.js

trait History extends js.Object {

  val item: String
  val params: js.UndefOr[js.Object]
}

object History {

  def apply(item: String, params: js.UndefOr[js.Object]): History = {
    js.Dynamic.literal(
      item = item,
      params = params
    ).asInstanceOf[History]
  }

  def unapply(arg: History): Option[(String, js.UndefOr[js.Object])] = {
    Some((
      arg.item,
      arg.params
    ))
  }
}
