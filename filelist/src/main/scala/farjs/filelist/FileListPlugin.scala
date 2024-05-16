package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, PanelStacks}
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FileListPlugin(val triggerKeys: js.Array[String] = js.Array()) extends js.Object {
  
  def onKeyTrigger(key: String,
                   stacks: PanelStacks,
                   data: js.UndefOr[js.Dynamic]): js.Promise[js.UndefOr[ReactClass]] = {
    
    js.Promise.resolve[js.UndefOr[ReactClass]](js.undefined)
  }

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {

    js.Promise.resolve[js.UndefOr[PanelStackItem[FileListState]]](js.undefined)
  }
}
