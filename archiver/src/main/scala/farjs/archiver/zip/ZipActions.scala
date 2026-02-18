package farjs.archiver.zip

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListItem

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../archiver/zip/ZipActions.mjs", JSImport.Default)
class ZipActions(api: ZipApi) extends FileListActions(js.native)

@js.native
@JSImport("../archiver/zip/ZipActions.mjs", JSImport.Default)
object ZipActions extends js.Object {
  
  var readZip: js.Function1[String, js.Promise[js.Map[String, js.Array[FileListItem]]]] = js.native
  var createApi: js.Function3[String, String, js.Promise[js.Map[String, js.Array[FileListItem]]], ZipApi] = js.native
}
