package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait FileListCapability extends js.Object

@js.native
@JSImport("@farjs/filelist/api/FileListCapability.mjs", JSImport.Default)
object FileListCapability extends js.Object {

  val read: FileListCapability = js.native
  val write: FileListCapability = js.native
  val delete: FileListCapability = js.native
  val mkDirs: FileListCapability = js.native
  val copyInplace: FileListCapability = js.native
  val moveInplace: FileListCapability = js.native
}
