package farjs.filelist

import farjs.filelist.stack.PanelStack

trait FileListPlugin {

  def triggerKey: String
  
  def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit
}
