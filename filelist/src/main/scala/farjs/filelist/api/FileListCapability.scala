package farjs.filelist.api

import scala.scalajs.js

sealed trait FileListCapability extends js.Object

object FileListCapability {

  val read: FileListCapability = "read".asInstanceOf[FileListCapability]
  val write: FileListCapability = "write".asInstanceOf[FileListCapability]
  val delete: FileListCapability = "delete".asInstanceOf[FileListCapability]
  val mkDirs: FileListCapability = "mkDirs".asInstanceOf[FileListCapability]
  val copyInplace: FileListCapability = "copyInplace".asInstanceOf[FileListCapability]
  val moveInplace: FileListCapability = "moveInplace".asInstanceOf[FileListCapability]
}
