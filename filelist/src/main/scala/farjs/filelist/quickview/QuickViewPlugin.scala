package farjs.filelist.quickview

import farjs.filelist.FileListPlugin
import farjs.filelist.stack.{PanelStack, PanelStackItem}

object QuickViewPlugin extends FileListPlugin {

  val triggerKey = "C-q"

  def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
    val compClass = QuickViewPanel()
    val exists =
      if (leftStack.peek.component == compClass) {
        leftStack.pop()
        true
      }
      else if (rightStack.peek.component == compClass) {
        rightStack.pop()
        true
      }
      else false
    
    if (!exists) {
      val stack =
        if (!isRight) rightStack
        else leftStack

      stack.push(PanelStackItem(
        component = compClass,
        dispatch = None,
        actions = None,
        state = Some(QuickViewParams())
      ))
    }
  }
}
