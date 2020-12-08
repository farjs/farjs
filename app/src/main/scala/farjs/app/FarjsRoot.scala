package farjs.app

import farjs.ui.LogPanel
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal._

class FarjsRoot(fileListComp: ReactClass,
                fileListPopups: ReactClass
               ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <.>()(
      <.box(
        ^.rbWidth := "70%"
      )(
        <(WithPortals())()(
          <.>()(
            Portal.create(
              <(fileListComp).empty
            ),
            <(fileListPopups).empty,
            <(FarjsTaskController()).empty
          )
        )
      ),
      <.box(
        ^.rbWidth := "30%",
        ^.rbHeight := "100%",
        ^.rbLeft := "70%"
      )(
        <(LogPanel())()()
        //<(ColorPanel())()()
      )
    )
  }
}
