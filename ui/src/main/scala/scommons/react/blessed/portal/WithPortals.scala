package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

private[portal] case class WithPortalsContext(onRender: js.Function2[Int, ReactElement, Unit],
                                              onRemove: js.Function1[Int, Unit])

object WithPortals extends FunctionComponent[Unit] {

  private[portal] val Context = ReactContext[WithPortalsContext](defaultValue = null)

  protected def render(props: Props): ReactElement = {
    val (portals, setPortals) = useStateUpdater(List.empty[(Int, ReactElement)])

    val onRender: js.Function2[Int, ReactElement, Unit] = useMemo({ () =>
      (id, el) => {
        setPortals { portals =>
          val updated = portals.map {
            case (pId, _) if pId == id => (id, el)
            case p => p
          }
          
          if (updated.exists(_._1 == id)) updated
          else portals :+ (id -> el)
        }
      }
    }, Nil)
    
    val onRemove: js.Function1[Int, Unit] = useMemo({ () =>
      id => {
        setPortals(portals => portals.filter(_._1 != id))
      }
    }, Nil)

    val lastPortalIndex = portals.size - 1
    
    <.>()(
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRender, onRemove))(
        props.children
      ),
      portals.zipWithIndex.map { case ((id, content), index) =>
        renderPortal(id, content, index == lastPortalIndex)
      }
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
