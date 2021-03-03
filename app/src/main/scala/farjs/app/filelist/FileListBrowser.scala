package farjs.app.filelist

import farjs.filelist._
import farjs.ui.menu.BottomMenu
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._
import scommons.react.hooks._
import scommons.react.blessed._

import scala.scalajs.js

case class FileListBrowserProps(dispatch: Dispatch,
                                actions: FileListActions,
                                data: FileListsStateDef)

object FileListBrowser extends FunctionComponent[FileListBrowserProps] {

  private[filelist] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[filelist] var bottomMenuComp: UiComponent[Unit] = BottomMenu

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val (isRight, setIsRight) = useStateUpdater(false)
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      val screen = elementRef.current.screen
      screen.key(js.Array("C-u"), { (_, _) =>
        setIsRight(!_)
        screen.focusNext()
      })
      ()
    }, Nil)

    <.>()(
      <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(
        <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions,
          if (isRight) props.data.right
          else props.data.left
        ))()
      ),
      <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(
        <(fileListPanelComp())(^.wrapped := FileListPanelProps(props.dispatch, props.actions,
          if (!isRight) props.data.right
          else props.data.left
        ))()
      ),

      <.box(
        ^.reactRef := elementRef,
        ^.rbTop := "100%-1"
      )(
        <(bottomMenuComp())()()
      )
    )
  }
}
