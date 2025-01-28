package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, WithStacksProps}
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.typedarray.Uint8Array

@js.native
@JSImport("@farjs/filelist/FileListPlugin.mjs", JSImport.Default)
class FileListPlugin(val triggerKeys: js.Array[String]) extends js.Object {
  
  def onKeyTrigger(key: String,
                   stacks: WithStacksProps,
                   data: js.UndefOr[js.Dynamic]): js.Promise[js.UndefOr[ReactClass]] = js.native

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = js.native
}
