package farjs.viewer

import farjs.filelist.theme.FileListTheme
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class ViewerSearchProps(searchTerm: String,
                             onComplete: () => Unit)

object ViewerSearch extends FunctionComponent[ViewerSearchProps] {
  
  private[viewer] var statusPopupComp: ReactClass = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    FileListTheme.useTheme
    val props = compProps.wrapped
    
    useLayoutEffect({ () =>
      //TODO: start search
      ()
    }, Nil)

    <(statusPopupComp)(^.plain := StatusPopupProps(
      text = s"""Searching for\n"${props.searchTerm}"""",
      title = "Search",
      onClose = { () =>
        // stop search
        props.onComplete()
        //inProgress.current = false
      }: js.Function0[Unit]
    ))()
  }
}
