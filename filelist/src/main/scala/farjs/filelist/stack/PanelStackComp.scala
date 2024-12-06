package farjs.filelist.stack

import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object PanelStackComp extends FunctionComponent[PanelStackProps] {
  
  val Context: ReactContext[PanelStackProps] = ReactContext[PanelStackProps](defaultValue = null)

  private[stack] var withSizeComp: ReactClass = WithSize

  def usePanelStack: PanelStackProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "PanelStackComp.Context is not found." +
          "\nPlease, make sure you use PanelStackComp.Context.Provider in parent component."
      ))
    }
    ctx
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val topComp = props.stack.peek().component

    <(withSizeComp)(^.plain := WithSizeProps({ (width, height) =>
      <(PanelStackComp.Context.Provider)(^.contextValue := PanelStackProps.copy(props)(width = width, height = height))(
        <(topComp)()(),
        compProps.children
      )
    }))()
  }
}
