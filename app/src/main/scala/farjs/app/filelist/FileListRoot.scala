package farjs.app.filelist

import farjs.filelist.FileListActions
import farjs.ui.Dispatch
import scommons.react._
import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../app/filelist/FileListRoot.mjs", JSImport.Default)
object FileListRoot extends js.Function3[Dispatch, FileListModule, ReactClass, ReactClass] {

  override def apply(dispatch: Dispatch,
                     module: FileListModule,
                     withPortalsComp: ReactClass
                    ): ReactClass = js.native
}

trait FolderShortcutsService extends js.Object {

  def getAll(): js.Promise[js.Array[js.UndefOr[String]]]

  def save(index: Int, path: String): js.Promise[Unit]

  def delete(index: Int): js.Promise[Unit]
}

trait FSServices extends js.Object {

  val folderShortcuts: FolderShortcutsService
}

@js.native
@JSImport("../fs/FSServices.mjs", JSImport.Default)
object FSServices extends js.Object {

  val Context: NativeContext = js.native

  def useServices(): FSServices = js.native
}

@js.native
@JSImport("../fs/FSFileListActions.mjs", JSImport.Default)
object FSFileListActions extends js.Object {

  val instance: FileListActions = js.native
}
