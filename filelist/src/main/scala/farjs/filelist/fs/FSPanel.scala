package farjs.filelist.fs

import farjs.filelist._
import scommons.react._
import scommons.react.blessed.BlessedScreen

object FSPanel extends FunctionComponent[FileListPanelProps] {

  private[fs] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "M-o" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(props.actions.openInDefaultApp(props.state.currDir.path, item.name))
          }
        case _ =>
          processed = false
      }

      processed
    }

    <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))()
  }
}
