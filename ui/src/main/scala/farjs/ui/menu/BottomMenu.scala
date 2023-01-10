package farjs.ui.menu

import farjs.ui._
import scommons.react._

case class BottomMenuProps(items: List[String])

object BottomMenu extends FunctionComponent[BottomMenuProps] {

  private[menu] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[menu] var bottomMenuViewComp: UiComponent[BottomMenuViewProps] = BottomMenuView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <(withSizeComp())(^.plain := WithSizeProps({ (width, _) =>
      
      <(bottomMenuViewComp())(^.wrapped := BottomMenuViewProps(
        width = width,
        items = props.items
      ))()
    }))()
  }
}
