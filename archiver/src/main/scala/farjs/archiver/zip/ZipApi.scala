package farjs.archiver.zip

import farjs.filelist.api._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../archiver/zip/ZipApi.mjs", JSImport.Default)
class ZipApi(
              val zipPath: String,
              val rootPath: String,
              protected var entriesByParentP: js.Promise[js.Map[String, js.Array[FileListItem]]]
) extends FileListApi(js.native, js.native)

@js.native
@JSImport("../archiver/zip/ZipApi.mjs", JSImport.Default)
object ZipApi extends js.Object {

  def addToZip(zipFile: String, parent: String, items: js.Set[String], onNextItem: js.Function0[Unit]): js.Promise[Unit] = js.native
  
  def readZip(zipPath: String): js.Promise[js.Map[String, js.Array[FileListItem]]] = js.native
}
