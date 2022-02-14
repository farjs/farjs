package farjs.filelist.stack

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class WithPanelStacksProps(leftStack: PanelStack, rightStack: PanelStack) {
  
  def activeStack: PanelStack =
    if (leftStack.isActive) leftStack
    else rightStack
}

object WithPanelStacks extends FunctionComponent[WithPanelStacksProps] {
  
  val Context: ReactContext[WithPanelStacksProps] = ReactContext[WithPanelStacksProps](defaultValue = null)

  def usePanelStacks: WithPanelStacksProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "WithPanelStacks.Context is not found." +
          "\nPlease, make sure you use WithPanelStacks and not creating nested stacks."
      ))
    }
    ctx
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    <(WithPanelStacks.Context.Provider)(^.contextValue := props)(
      compProps.children
    )
  }
}
