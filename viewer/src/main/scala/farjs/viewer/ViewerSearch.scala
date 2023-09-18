package farjs.viewer

import farjs.file.popups._
import farjs.filelist.theme.FileListTheme
import scommons.react._

case class ViewerSearchProps(showSearchPopup: Boolean,
                             onHideSearchPopup: () => Unit)

object ViewerSearch extends FunctionComponent[ViewerSearchProps] {
  
  private[viewer] var textSearchPopup: UiComponent[TextSearchPopupProps] = TextSearchPopup

  protected def render(compProps: Props): ReactElement = {
    FileListTheme.useTheme
    val props = compProps.wrapped
    
    <.>()(
      if (props.showSearchPopup) Some {
        <(textSearchPopup())(^.wrapped := TextSearchPopupProps(
          onSearch = { _ =>
            //TODO: implement
          },
          onCancel = props.onHideSearchPopup
        ))()
      }
      else None
    )
  }
}
