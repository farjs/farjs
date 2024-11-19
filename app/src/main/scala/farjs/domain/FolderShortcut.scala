package farjs.domain

import scala.scalajs.js

trait FolderShortcut extends js.Object {

  val id: Int
  val path: String
}

object FolderShortcut {

  def apply(id: Int, path: String): FolderShortcut = {
    js.Dynamic.literal(
      id = id,
      path = path
    ).asInstanceOf[FolderShortcut]
  }

  def unapply(arg: FolderShortcut): Option[(Int, String)] = {
    Some((
      arg.id,
      arg.path
    ))
  }
}
