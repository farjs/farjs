package farjs.archiver

import farjs.filelist._
import farjs.filelist.stack.{PanelStackItem, WithStacksProps}
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.Uint8Array

@js.native
@JSImport("../archiver/ArchiverPlugin.mjs", JSImport.Default)
class ArchiverPlugin extends FileListPlugin(js.native) {

  override def onKeyTrigger(key: String,
                            stacks: WithStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = js.native
  
  override def onFileTrigger(filePath: String,
                             fileHeader: Uint8Array,
                             onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = js.native
}

@js.native
@JSImport("../archiver/ArchiverPlugin.mjs", JSImport.Default)
object ArchiverPlugin extends js.Object {

  val instance: ArchiverPlugin = js.native
}
