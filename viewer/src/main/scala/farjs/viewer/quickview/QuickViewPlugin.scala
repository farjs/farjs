package farjs.viewer.quickview

import farjs.filelist.FileListPlugin
import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

object QuickViewPlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("C-q")

  private val panelComp: ReactClass = QuickViewPanel()

  override def onKeyTrigger(key: String,
                            stacks: WithPanelStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Future[Option[ReactClass]] = {
    val exists =
      if (stacks.left.stack.peek.component == panelComp) {
        stacks.left.stack.pop()
        true
      }
      else if (stacks.right.stack.peek.component == panelComp) {
        stacks.right.stack.pop()
        true
      }
      else false
    
    if (!exists) {
      val stack = WithPanelStacksProps.nonActive(stacks).stack
      stack.push(PanelStackItem[QuickViewParams](
        component = panelComp,
        dispatch = js.undefined,
        actions = js.undefined,
        state = QuickViewParams()
      ))
    }
    
    Future.successful(None)
  }
}
