package farjs.archiver.zip

import farjs.filelist.api.FileListItem
import scommons.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../archiver/zip/ZipPanel.mjs", JSImport.Default)
object ZipPanel extends js.Function4[String, String, js.Promise[js.Map[String, js.Array[FileListItem]]], js.Function0[Unit], ReactClass] {

  override def apply(zipPath: String,
                     rootPath: String,
                     entriesByParentF: js.Promise[js.Map[String, js.Array[FileListItem]]],
                     onClose: js.Function0[Unit]
                    ): ReactClass = js.native
}
