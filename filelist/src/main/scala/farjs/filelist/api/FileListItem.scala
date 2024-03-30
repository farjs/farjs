package farjs.filelist.api

case class FileListItem(name: String,
                        isDir: Boolean = false,
                        isSymLink: Boolean = false,
                        size: Double = 0.0,
                        atimeMs: Double = 0.0,
                        mtimeMs: Double = 0.0,
                        ctimeMs: Double = 0.0,
                        birthtimeMs: Double = 0.0,
                        permissions: String = "" //optional, format: drwx---rwx
                       ) {
  
  lazy val nameNormalized: String = name.toLowerCase

  lazy val ext: String = {
    val dotIndex = name.lastIndexOf('.')
    if (dotIndex != -1) name.drop(dotIndex + 1)
    else name
  }
  lazy val extNormalized: String = ext.toLowerCase
}

object FileListItem {

  val up: FileListItem = FileListItem("..", isDir = true)
  val currDir: FileListItem = FileListItem(".", isDir = true)
}
