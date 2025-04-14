package farjs.fs

import farjs.fs.popups.FolderShortcutsService
import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait FSServices extends js.Object {

  val folderShortcuts: FolderShortcutsService
}

@js.native
@JSImport("../fs/FSServices.mjs", JSImport.Default)
object FSServices extends js.Object {

  val Context: NativeContext = js.native
  
  def useServices(): FSServices = js.native
}
