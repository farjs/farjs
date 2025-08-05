package farjs.fs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait FSService extends js.Object {

  def openItem(parent: String, item: String): js.Promise[Unit]

  def readDisk(path: String): js.Promise[js.UndefOr[FSDisk]]

  def readDisks(): js.Promise[js.Array[FSDisk]]
}

@js.native
@JSImport("../fs/FSService.mjs", JSImport.Default)
object FSService extends js.Object {

  val instance: FSService = js.native
}
