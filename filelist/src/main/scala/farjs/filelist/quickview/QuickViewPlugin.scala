package farjs.filelist.quickview

import farjs.filelist.FileListPlugin
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass

object QuickViewPlugin extends FileListPlugin {

  override val triggerKey: Option[String] = Some("C-q")

  private val component: ReactClass = QuickViewPanel()

  override def onKeyTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
    val exists =
      if (leftStack.peek.component == component) {
        leftStack.pop()
        true
      }
      else if (rightStack.peek.component == component) {
        rightStack.pop()
        true
      }
      else false
    
    if (!exists) {
      val stack =
        if (!isRight) rightStack
        else leftStack

      stack.push(PanelStackItem(
        component = component,
        dispatch = None,
        actions = None,
        state = Some(QuickViewParams())
      ))
    }
  }
}
