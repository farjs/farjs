package farjs.filelist.api

case class FileListDir(path: String,
                       isRoot: Boolean,
                       items: Seq[FileListItem]) {
  
  def isEmpty: Boolean = path.isEmpty
}

object FileListDir {

  val curr = "."
}
