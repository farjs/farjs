package farjs.filelist.api

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/api/FileListApi.mjs", JSImport.Default)
class FileListApi(val isLocal: Boolean,
                  val capabilities: js.Set[FileListCapability]) extends js.Object {

  def readDir(path: String, dir: js.UndefOr[String]): js.Promise[FileListDir] = js.native

  def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit] = js.native

  def mkDirs(dirs: js.Array[String]): js.Promise[String] = js.native

  def readFile(parent: String,
               item: FileListItem,
               position: Double): js.Promise[FileSource] = js.native

  def writeFile(parent: String,
                fileName: String,
                onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]
               ): js.Promise[js.UndefOr[FileTarget]] = js.native

  def getDriveRoot(path: String): js.Promise[js.UndefOr[String]] = js.native
}
