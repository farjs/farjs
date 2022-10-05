package farjs.app.filelist.fs

import scommons.react._
import scommons.react.hooks._

case class FSFoldersHistoryProps(showPopup: Boolean,
                                 currDirPath: String,
                                 onChangeDir: String => Unit,
                                 onHidePopup: () => Unit)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  private[fs] var fsFoldersPopup: UiComponent[FSFoldersPopupProps] = FSFoldersPopup
  
  protected def render(compProps: Props): ReactElement = {
    val (dirs, setDirs) = useState(List.empty[String])
    val props = compProps.wrapped
    val currDirPath = props.currDirPath

    useLayoutEffect({ () =>
      if (currDirPath.nonEmpty) {
        val index = dirs.indexOf(currDirPath)
        val (prefix, suffix) =
          if (index >= 0) (dirs.take(index), dirs.drop(index + 1))
          else (dirs, Nil)

        setDirs(prefix ++: suffix :+ currDirPath)
      }
      ()
    }, List(currDirPath))

    if (props.showPopup) {
      <(fsFoldersPopup())(^.wrapped := FSFoldersPopupProps(
        selected =
          if (dirs.isEmpty) 0
          else dirs.length - 1,
        items = dirs,
        onAction = { index =>
          if (dirs.nonEmpty) {
            props.onChangeDir(dirs(index))
          }
        },
        onClose = props.onHidePopup
      ))()
    }
    else null
  }
}
