package farjs.viewer.quickview

import farjs.filelist.theme.FileListTheme
import farjs.ui._
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import farjs.ui.task.{Task, TaskAction}
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

object QuickViewDir extends FunctionComponent[QuickViewDirProps] {

  private[quickview] var statusPopupComp: ReactClass = StatusPopup
  private[quickview] var textLineComp: ReactClass = TextLine

  protected def render(compProps: Props): ReactElement = {
    val (showPopup, setShowPopup) = useState(false)
    val inProgress = useRef(false)

    val props = compProps.plain
    val stack = props.stack
    val params = stack.params[QuickViewParams]()
    val theme = FileListTheme.useTheme().fileList

    def scanDir(): Unit = {
      val parent = props.state.currDir.path
      val currItems = js.Array(props.currItem)
      val params = QuickViewParams(props.currItem.name, parent)
      stack.update[QuickViewParams](_.withState(params))

      var folders = 0d
      var files = 0d
      var filesSize = 0d
      val resultF = props.actions.scanDirs(parent, currItems, onNextDir = { (_, items) =>
        items.foreach { i =>
          if (i.isDir) folders += 1
          else {
            files += 1
            filesSize += i.size
          }
        }
        inProgress.current
      }).toFuture
      resultF.onComplete {
        case Success(false) => // already cancelled
        case Success(true) =>
          setShowPopup(false)
          val newParams = params.copy(folders = folders, files = files, filesSize = filesSize)
          stack.update[QuickViewParams](_.withState(newParams))
        case Failure(_) =>
          setShowPopup(false)
          props.dispatch(TaskAction(Task("Quick view dir scan", resultF)))
      }
    }

    useLayoutEffect({ () =>
      if (!inProgress.current && showPopup) { // start scan
        inProgress.current = true
        scanDir()
      } else if (inProgress.current && !showPopup) { // stop scan
        inProgress.current = false
      }
      ()
    }, List(showPopup))

    useLayoutEffect({ () =>
      val params = stack.params[QuickViewParams]()
      if (params.name != props.currItem.name || params.parent != props.state.currDir.path) {
        setShowPopup(true)
      }
    }, List(props.currItem.name, props.state.currDir.path, stack.asInstanceOf[js.Any]))

    <.>()(
      if (showPopup) Some(
        <(statusPopupComp)(^.plain := StatusPopupProps(
          text = s"Scanning the folder\n${props.currItem.name}",
          title = "View Dir",
          onClose = { () =>
            setShowPopup(false)
          }: js.Function0[Unit]
        ))()
      ) else None,

      <.text(
        ^.rbLeft := 2,
        ^.rbTop := 2,
        ^.rbStyle := theme.regularItem,
        ^.content :=
          """Folder
            |
            |Contains:
            |
            |Folders
            |Files
            |Files size""".stripMargin
      )(),

      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = 12,
        top = 2,
        width = props.width - 14,
        text = s""""${props.currItem.name}"""",
        style = theme.regularItem,
        padding = 0
      ))(),

      <.text(
        ^.rbLeft := 15,
        ^.rbTop := 6,
        ^.rbStyle := theme.selectedItem,
        ^.content :=
          f"""${params.folders}%,.0f
             |${params.files}%,.0f
             |${params.filesSize}%,.0f""".stripMargin
      )()
    )
  }
}
