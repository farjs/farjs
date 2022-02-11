package farjs.filelist.quickview

import farjs.filelist._
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

class QuickViewPlugin(actions: FileListActions)
  extends BaseStateController[FileListsGlobalState, QuickViewPanelProps] with FileListPlugin {

  lazy val uiComponent: UiComponent[QuickViewPanelProps] = QuickViewPanel

  val triggerKey = "C-q"

  def onTrigger(isRight: Boolean, leftStack: PanelStack, rightStack: PanelStack): Unit = {
    val compClass = apply()
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

  def mapStateToProps(dispatch: Dispatch, state: FileListsGlobalState, props: Props[Unit]): QuickViewPanelProps = {
    QuickViewPanelProps(
      dispatch = dispatch,
      actions = actions,
      data = state.fileListsState
    )
  }
}
