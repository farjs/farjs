package farjs.filelist.api

case class FileListDir(path: String,
                       isRoot: Boolean,
                       items: Seq[FileListItem])

object FileListDir {

  val curr = "."
}
