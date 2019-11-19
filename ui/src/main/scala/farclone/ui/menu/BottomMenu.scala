package farclone.ui.menu

import farclone.ui._
import scommons.react._

object BottomMenu extends FunctionComponent[Unit] {
  
  protected def render(compProps: Props): ReactElement = {

    <(WithSize())(^.wrapped := WithSizeProps({ (width, _) =>
      
      <(BottomMenuView())(^.wrapped := BottomMenuViewProps(
        width = width,
        items = items
      ))()
    }))()
  }

  private[ui] val items = List(
    /*  F1 */ "",
    /*  F2 */ "",
    /*  F3 */ "",
    /*  F4 */ "",
    /*  F5 */ "",
    /*  F6 */ "",
    /*  F7 */ "",
    /*  F8 */ "Del",
    /*  F9 */ "",
    /* F10 */ "Exit",
    /* F11 */ "",
    /* F12 */ ""
  )
}
