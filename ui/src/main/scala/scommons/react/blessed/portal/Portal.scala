package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js
import scala.scalajs.js.{Error, JavaScriptException}

private[portal] case class PortalProps(content: ReactElement)

private[portal] case class PortalContext(onAdd: js.Function1[ReactElement, Int],
                                         onRemove: js.Function1[Int, Unit])

object Portal extends FunctionComponent[PortalProps] {
  
  private[portal] val Context = ReactContext[PortalContext](defaultValue = null)
  
  def create(content: ReactElement): ReactElement = {
    <(Portal())(^.wrapped := PortalProps(content))()
  }
  
  protected def render(compProps: Props): ReactElement = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw JavaScriptException(Error(
        "PortalContext is not specified, use WithPortals to wrap your root component"
      ))
    }
    
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      val portalId = ctx.onAdd(props.content)
      () => {
        ctx.onRemove(portalId)
      }
    }, List(props.content))

    <.>()()
  }
}
