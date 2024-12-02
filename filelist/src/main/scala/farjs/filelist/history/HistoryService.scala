package farjs.filelist.history

import scala.scalajs.js

trait HistoryService extends js.Object {

  def getAll(): js.Promise[js.Array[History]]

  def getOne(item: String): js.Promise[js.UndefOr[History]]

  def save(h: History): js.Promise[Unit]
}
