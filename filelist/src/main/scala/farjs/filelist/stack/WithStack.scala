package farjs.filelist.stack

import farjs.ui.{WithSize, WithSizeProps}
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object WithStack extends FunctionComponent[WithStackProps] {
  
  val Context: ReactContext[WithStackProps] = ReactContext[WithStackProps](defaultValue = null)

  private[stack] var withSizeComp: ReactClass = WithSize

  def useStack(): WithStackProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "WithStack.Context is not found." +
          "\nPlease, make sure you use WithStack.Context.Provider in parent component."
      ))
    }
    ctx
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val topComp = props.stack.peek().component

    <(withSizeComp)(^.plain := WithSizeProps({ (width, height) =>
      <(WithStack.Context.Provider)(^.contextValue := WithStackProps.copy(props)(width = width, height = height))(
        <(topComp)()(),
        compProps.children
      )
    }))()
  }
}
