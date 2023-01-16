package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

trait FileListPlugin {

  val triggerKeys: js.Array[String] = js.Array()
  
  def onKeyTrigger(key: String, stacks: WithPanelStacksProps): Option[ReactClass] = None

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: () => Unit): Option[PanelStackItem[FileListState]] = None
}
