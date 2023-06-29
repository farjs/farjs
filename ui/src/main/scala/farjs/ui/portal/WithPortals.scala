package farjs.ui.portal

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

private[portal] case class WithPortalsContext(onRender: js.Function2[Int, ReactElement, Unit],
                                              onRemove: js.Function1[Int, Unit])

object WithPortals {

  private[portal] val Context = ReactContext[WithPortalsContext](defaultValue = null)
}

class WithPortals(screen: BlessedScreen) extends FunctionComponent[Unit] {

  protected def render(props: Props): ReactElement = {
    val (portals, setPortals) = useStateUpdater(List.empty[(Int, ReactElement, BlessedElement)])

    val context = useMemo({ () =>

      val onRender: js.Function2[Int, ReactElement, Unit] = { (id, el) =>
        setPortals { portals =>
          val updated = portals.map {
            case (pId, _, focused) if pId == id => (id, el, focused)
            case p => p
          }
          
          if (updated.exists(_._1 == id)) updated
          else {
            portals :+ ((id, el, screen.focused))
          }
        }
      }
    
      val onRemove: js.Function1[Int, Unit] = { id =>
        setPortals { portals =>
          val index = portals.indexWhere(_._1 == id)
          if (index >= 0) {
            val (_, _, focused) = portals(index)
            val updated =
              if (index == portals.size - 1) {
                if (!js.isUndefined(focused) && focused != null) {
                  focused.focus()
                }
                portals
              } else {
                val (pid, el, _) = portals(index + 1)
                portals.updated(index + 1, (pid, el, focused))
              }
            
            Future(screen.render()) //trigger re-render on the next tick
            updated.filter(_._1 != id)
          }
          else portals
        }
      }

      WithPortalsContext(onRender, onRemove)
    }, Nil)

    val lastPortalIndex = portals.size - 1
    
    <.>()(
      <(WithPortals.Context.Provider)(^.contextValue := context)(
        props.children
      ),
      <(WithPortals.Context.Provider)(^.contextValue := context)(
        portals.zipWithIndex.map { case ((id, content, _), index) =>
          renderPortal(id, content, index == lastPortalIndex)
        }
      )
    )
  }
  
  private[portal] def renderPortal(id: Int, content: ReactElement, isActive: Boolean): ReactElement = {
    <.>(^.key := s"$id")(
      <(Portal.Context.Provider)(^.contextValue := PortalContext(isActive))(
        content
      )
    )
  }
}
