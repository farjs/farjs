package farjs.ui.menu

import farjs.ui._
import scommons.react._

import scala.scalajs.js

case class BottomMenuProps(items: List[String])

object BottomMenu extends FunctionComponent[BottomMenuProps] {

  private[menu] var withSizeComp: ReactClass = WithSize
  private[menu] var bottomMenuViewComp: UiComponent[BottomMenuViewProps] = BottomMenuView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <(withSizeComp)(^.plain := WithSizeProps({ (width, _) =>
      
      <(bottomMenuViewComp())(^.plain := BottomMenuViewProps(
        width = width,
        items = js.Array(props.items: _*)
      ))()
    }))()
  }
}
