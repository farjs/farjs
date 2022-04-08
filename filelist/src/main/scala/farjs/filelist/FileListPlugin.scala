package farjs.filelist

import farjs.filelist.stack.{PanelStack, PanelStackItem}

import scala.scalajs.js.typedarray.Uint8Array

trait FileListPlugin {

  val triggerKey: Option[String] = None
  
  def onKeyTrigger(leftStack: PanelStack, rightStack: PanelStack): Unit = ()

  def onFileTrigger(filePath: String,
                    fileHeader: Uint8Array,
                    onClose: () => Unit): Option[PanelStackItem[FileListState]] = None
}
