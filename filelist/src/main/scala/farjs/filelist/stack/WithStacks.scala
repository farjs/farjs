package farjs.filelist.stack

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object WithStacks extends FunctionComponent[WithStacksProps] {

  val Context: ReactContext[WithStacksProps] = ReactContext[WithStacksProps](defaultValue = null)
  
  def useStacks(): WithStacksProps = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "WithStacks.Context is not found." +
          "\nPlease, make sure you use WithStacks.Context.Provider in parent component."
      ))
    }
    ctx
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <(WithStacks.Context.Provider)(^.contextValue := props)(
      compProps.children
    )
  }
}
