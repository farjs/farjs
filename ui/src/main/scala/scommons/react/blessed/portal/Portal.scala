package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js
import scala.scalajs.js.{Error, JavaScriptException}

private[portal] case class PortalProps(content: ReactElement)

private[portal] case class PortalContext(onRender: js.Function2[Int, ReactElement, Unit],
                                         onRemove: js.Function1[Int, Unit])

object Portal extends FunctionComponent[PortalProps] {
  
  private[portal] val Context = ReactContext[PortalContext](defaultValue = null)
  
  def create(content: ReactElement): ReactElement = {
    <(Portal())(^.wrapped := PortalProps(content))()
  }
  
  protected def render(compProps: Props): ReactElement = {
    val (portalId, _) = useState(() => getNextPortalId)
    val ctx = useContext(Context)
    if (ctx == null) {
      throw JavaScriptException(Error(
        "PortalContext is not specified, use WithPortals to wrap your root component"
      ))
    }
    
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      val cleanup = () => {
        ctx.onRemove(portalId)
      }
      cleanup
    }, Nil)

    useLayoutEffect({ () =>
      ctx.onRender(portalId, props.content)
    }, List(props.content))

    <.>()()
  }

  private[portal] var nextPortalId = 0
  private def getNextPortalId: Int = {
    nextPortalId += 1
    println(s"nextPortalId: $nextPortalId")
    nextPortalId
  }
}
