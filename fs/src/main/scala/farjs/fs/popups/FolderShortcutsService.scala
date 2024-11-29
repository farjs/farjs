package farjs.fs.popups

import scala.scalajs.js

trait FolderShortcutsService extends js.Object {

  def getAll(): js.Promise[js.Array[js.UndefOr[String]]]

  def save(index: Int, path: String): js.Promise[Unit]

  def delete(index: Int): js.Promise[Unit]
}
