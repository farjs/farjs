package farjs.viewer

import farjs.filelist._
import farjs.ui.popup.{Popup, PopupProps}
import farjs.ui.theme.Theme
import farjs.viewer.ViewerPluginUi._
import scommons.react._
import scommons.react.blessed._

class ViewerPluginUi(filePath: String)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
      <.text(
        ^.rbWidth := "100%",
        ^.rbHeight := 1,
        ^.rbStyle := headerStyle,
        ^.content := filePath
      )(),

      <.button(
        ^.rbMouse := true,
        ^.rbTop := 1,
        ^.rbWidth := "100%",
        ^.rbHeight := "100%",
        ^.rbStyle := contentStyle,
        ^.content := ""
      )()
    )
  }
}

object ViewerPluginUi {

  private[viewer] var popupComp: UiComponent[PopupProps] = Popup

  private[viewer] lazy val headerStyle: BlessedStyle = {
    val style = Theme.current.menu.item
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }

  private[viewer] lazy val contentStyle: BlessedStyle = {
    val style = Theme.current.fileList.regularItem
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
}
