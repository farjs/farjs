package farjs.app.filelist.fs

import scommons.react._

case class FSFoldersHistoryProps(showPopup: Boolean,
                                 onHidePopup: () => Unit)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  private[fs] var fsFoldersPopup: UiComponent[FSFoldersPopupProps] = FSFoldersPopup
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    if (props.showPopup) {
      <(fsFoldersPopup())(^.wrapped := FSFoldersPopupProps(
        selected = 0,
        items = Nil,
        onAction = { _ =>
          //TODO
        },
        onClose = props.onHidePopup
      ))()
    }
    else null
  }
}
