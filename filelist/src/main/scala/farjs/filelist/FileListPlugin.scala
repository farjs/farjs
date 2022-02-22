package farjs.filelist

import farjs.filelist.stack.PanelStack

trait FileListPlugin {

  val triggerKey: Option[String] = None
  
  def onKeyTrigger(leftStack: PanelStack, rightStack: PanelStack): Unit = ()
}
