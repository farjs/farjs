package farjs.app.filelist.fs

import farjs.ui._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class FSFoldersPopupProps(selected: Int,
                               items: List[String],
                               onAction: String => Unit,
                               onClose: () => Unit)

object FSFoldersPopup extends FunctionComponent[FSFoldersPopupProps] {

  private[fs] var popupComp: UiComponent[PopupProps] = Popup
  private[fs] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[fs] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.current.popup.menu

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
      <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
        val modalHeight = height - 4
        val contentWidth = width - paddingHorizontal * 2 - 2
        val contentHeight = modalHeight - paddingVertical * 2 - 2

        <(modalContentComp())(^.wrapped := ModalContentProps(
          title = "Folders history",
          size = (width, modalHeight),
          style = theme,
          padding = padding
        ))(
          <.button(
            ^.rbMouse := true,
            ^.rbLeft := 1,
            ^.rbTop := 1,
            ^.rbWidth := contentWidth,
            ^.rbHeight := contentHeight
          )(
            <.text(
              ^.rbWidth := contentWidth,
              ^.rbHeight := contentHeight,
              ^.rbStyle := theme,
              ^.content := "test"
            )()
          )
        )
      })()
    )
  }

  private[fs] val paddingHorizontal = 2
  private[fs] val paddingVertical = 1

  private[fs] val padding: BlessedPadding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
  }
}
