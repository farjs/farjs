package farjs.filelist.api

import scala.scalajs.js

sealed trait FileListDir extends js.Object {
  val path: String
  val isRoot: Boolean
  val items: js.Array[FileListItem]
}

object FileListDir {

  def apply(path: String,
            isRoot: Boolean,
            items: js.Array[FileListItem]): FileListDir = {

    js.Dynamic.literal(
      path = path,
      isRoot = isRoot,
      items = items
    ).asInstanceOf[FileListDir]
  }

  def unapply(arg: FileListDir): Option[(String, Boolean, js.Array[FileListItem])] = {
    Some((
      arg.path,
      arg.isRoot,
      arg.items
    ))
  }

  def copy(p: FileListDir)(path: String = p.path,
                           isRoot: Boolean = p.isRoot,
                           items: js.Array[FileListItem] = p.items): FileListDir = {

    FileListDir(
      path = path,
      isRoot = isRoot,
      items = items
    )
  }
}
