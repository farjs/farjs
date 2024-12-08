package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, WithStacksProps}
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FileListPlugin(val triggerKeys: js.Array[String] = js.Array()) extends js.Object {
  
  def onKeyTrigger(key: String,
                   stacks: WithStacksProps,
                   data: js.UndefOr[js.Dynamic]): js.Promise[js.UndefOr[ReactClass]] = {
    
    js.Promise.resolve[js.UndefOr[ReactClass]](js.undefined)
  }

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {

    js.Promise.resolve[js.UndefOr[PanelStackItem[FileListState]]](js.undefined)
  }
}
