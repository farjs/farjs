package farjs.filelist.stack

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class WithPanelStacksProps(left: PanelStackData,
                                right: PanelStackData)

object WithPanelStacksProps {

  def active(stacks: WithPanelStacksProps): PanelStackData =
    if (stacks.left.stack.isActive) stacks.left
    else stacks.right

  def nonActive(stacks: WithPanelStacksProps): PanelStackData =
    if (!stacks.left.stack.isActive) stacks.left
    else stacks.right
}

object WithPanelStacks extends FunctionComponent[WithPanelStacksProps] {
  
  val Context: ReactContext[WithPanelStacksProps] = ReactContext[WithPanelStacksProps](defaultValue = null)

  def usePanelStacks: WithPanelStacksProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "WithPanelStacks.Context is not found." +
          "\nPlease, make sure you use WithPanelStacks.Context.Provider in parent component."
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
