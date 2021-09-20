package farjs.ui.menu

import farjs.ui._
import scommons.react._

object BottomMenu extends FunctionComponent[Unit] {

  private[menu] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[menu] var bottomMenuViewComp: UiComponent[BottomMenuViewProps] = BottomMenuView
  
  protected def render(compProps: Props): ReactElement = {

    <(withSizeComp())(^.wrapped := WithSizeProps({ (width, _) =>
      
      <(bottomMenuViewComp())(^.wrapped := BottomMenuViewProps(
        width = width,
        items = items
      ))()
    }))()
  }

  private[ui] val items = List(
    /*  F1 */ "",
    /*  F2 */ "",
    /*  F3 */ "View",
    /*  F4 */ "",
    /*  F5 */ "Copy",
    /*  F6 */ "Move/Ren",
    /*  F7 */ "MkFolder",
    /*  F8 */ "Delete",
    /*  F9 */ "",
    /* F10 */ "Exit",
    /* F11 */ "",
    /* F12 */ "DevTools"
  )
}
