package farjs.filelist

import farjs.filelist.stack.{PanelStack, PanelStackItem}

trait FileListPlugin {

  val triggerKey: Option[String] = None
  
  def onKeyTrigger(leftStack: PanelStack, rightStack: PanelStack): Unit = ()

  def onFileTrigger(filePath: String, onClose: () => Unit): Option[PanelStackItem[FileListState]] = None
}
