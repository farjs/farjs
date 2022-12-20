package farjs.filelist

import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.react.ReactClass

import scala.scalajs.js.typedarray.Uint8Array

trait FileListPlugin {

  val triggerKey: Option[String] = None
  
  def onKeyTrigger(stacks: WithPanelStacksProps): Option[ReactClass] = None

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: () => Unit): Option[PanelStackItem[FileListState]] = None
}
