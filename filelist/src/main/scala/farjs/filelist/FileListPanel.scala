package farjs.filelist

import farjs.ui._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._

case class FileListPanelProps(dispatch: Dispatch,
                              actions: FileListActions,
                              state: FileListState)

object FileListPanel extends FunctionComponent[FileListPanelProps] {

  private[filelist] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[filelist] var fileListPanelView: UiComponent[FileListPanelViewProps] = FileListPanelView

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <(withSizeComp())(^.wrapped := WithSizeProps({ (width, height) =>
      <(fileListPanelView())(^.wrapped := FileListPanelViewProps(
        dispatch = props.dispatch,
        actions = props.actions,
        state = props.state,
        width = width,
        height = height
      ))()
    }))()
  }
}
