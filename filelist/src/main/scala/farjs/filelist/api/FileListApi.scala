package farjs.filelist.api

import scala.scalajs.js

trait FileListApi extends js.Object {

  def capabilities: js.Set[FileListCapability]

  def readDir(path: String, dir: js.UndefOr[String]): js.Promise[FileListDir]

  def delete(parent: String, items: js.Array[FileListItem]): js.Promise[Unit]

  def mkDirs(dirs: js.Array[String]): js.Promise[String]

  def readFile(parent: String,
               item: FileListItem,
               position: Double): js.Promise[FileSource]

  def writeFile(parent: String,
                fileName: String,
                onExists: FileListItem => js.Promise[js.UndefOr[Boolean]]
               ): js.Promise[js.UndefOr[FileTarget]]
}
