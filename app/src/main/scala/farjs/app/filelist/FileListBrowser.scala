package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.ui.menu.BottomMenu
import scommons.react._
import scommons.react.blessed._

class FileListBrowser(leftPanelComp: ReactClass,
                      rightPanelComp: ReactClass
                      ) extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <.>()(
      <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(
        <(leftPanelComp).empty
      ),
      <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(
        <(rightPanelComp).empty
      ),

      <.box(^.rbTop := "100%-1")(
        <(bottomMenuComp())()()
      )
    )
  }
}

object FileListBrowser {

  private[filelist] var bottomMenuComp: UiComponent[Unit] = BottomMenu
}
