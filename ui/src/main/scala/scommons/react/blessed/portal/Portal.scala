package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js.{Error, JavaScriptException}

case class PortalProps(content: ReactElement)

case class PortalContext(isActive: Boolean)

object Portal extends FunctionComponent[PortalProps] {

  val Context: ReactContext[PortalContext] = ReactContext(
    defaultValue = PortalContext(isActive = false)
  )
  
  protected def render(compProps: Props): ReactElement = {
    val (portalId, _) = useState(() => getNextPortalId)
    
    val ctx = useContext(WithPortals.Context)
    if (ctx == null) {
      throw JavaScriptException(Error(
        "WithPortals.Context is not found." +
          "\nPlease, make sure you use WithPortals and not creating nested portals."
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
    //println(s"nextPortalId: $nextPortalId")
    nextPortalId
  }
}
