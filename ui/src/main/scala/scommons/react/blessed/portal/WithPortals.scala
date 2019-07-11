package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object WithPortals extends FunctionComponent[Unit] {

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

    <(Portal.Context.Provider)(^.contextValue := PortalContext(onRender, onRemove))(
      props.children,
      
      portals.map { case (id, content) =>
        renderPortal(id, content)
      }
    )
  }
  
  private[portal] def renderPortal(id: Int, content: ReactElement): ReactElement = {
    <.>(
      ^.key := s"$id"
    )(content)
  }
}
