package farjs.ui.menu

import farjs.ui._
import scommons.react._

object BottomMenu extends FunctionComponent[BottomMenuProps] {

  private[menu] var withSizeComp: ReactClass = WithSize
  private[menu] var bottomMenuViewComp: ReactClass = BottomMenuView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <(withSizeComp)(^.plain := WithSizeProps({ (width, _) =>
      
      <(bottomMenuViewComp)(^.plain := BottomMenuViewProps(
        width = width,
        items = props.items
      ))()
    }))()
  }
}
