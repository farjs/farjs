package farjs.viewer.quickview

import farjs.filelist.FileListPlugin
import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.react.ReactClass

object QuickViewPlugin extends FileListPlugin {

  override val triggerKey: Option[String] = Some("C-q")

  private val panelComp: ReactClass = QuickViewPanel()

  override def onKeyTrigger(stacks: WithPanelStacksProps): Option[ReactClass] = {
    val exists =
      if (stacks.leftStack.peek.component == panelComp) {
        stacks.leftStack.pop()
        true
      }
      else if (stacks.rightStack.peek.component == panelComp) {
        stacks.rightStack.pop()
        true
      }
      else false
    
    if (!exists) {
      val stack =
        if (stacks.leftStack.isActive) stacks.rightStack
        else stacks.leftStack

      stack.push(PanelStackItem(
        component = panelComp,
        dispatch = None,
        actions = None,
        state = Some(QuickViewParams())
      ))
    }
    
    None
  }
}
