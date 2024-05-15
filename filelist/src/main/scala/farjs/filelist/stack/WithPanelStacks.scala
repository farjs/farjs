package farjs.filelist.stack

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object WithPanelStacks extends FunctionComponent[PanelStacks] {

  val Context: ReactContext[PanelStacks] = ReactContext[PanelStacks](defaultValue = null)
  
  def usePanelStacks: PanelStacks = {
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
    val props = compProps.plain
    
    <(WithPanelStacks.Context.Provider)(^.contextValue := props)(
      compProps.children
    )
  }
}
