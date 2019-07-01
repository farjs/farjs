package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object WithPortals extends FunctionComponent[Unit] {

  protected def render(props: Props): ReactElement = {
    val (portals, setPortals) = useStateUpdater(List.empty[(Int, ReactElement)])

    val onPortalAdd: js.Function1[ReactElement, Int] = useMemo({ () =>
      el => {
        var id = 0
        setPortals { portals =>
          id = getNextPortalId(portals)
          portals :+ (id -> el)
        }
        println(s"nextPortalId: $id")
        id
      }
    }, Nil)
    
    val onPortalRemove: js.Function1[Int, Unit] = useMemo({ () =>
      id => {
        setPortals(portals => portals.filter(_._1 != id))
      }
    }, Nil)

    <(Portal.Context.Provider)(^.contextValue := PortalContext(onPortalAdd, onPortalRemove))(
      props.children,
      
      portals.map(_._2)
    )
  }

  private var nextPortalId = 0

  private[portal] def getNextPortalId(portals: List[(Int, ReactElement)]): Int = {
    do {
      nextPortalId += 1
    } while (portals.exists(_._1 == nextPortalId))
    
    nextPortalId
  }
}
