package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.{FileListPanel, FileListPanelProps}
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

class ZipPanel(rootPath: String,
               entriesF: Future[List[ZipEntry]],
               onClose: () => Unit
              ) extends FunctionComponent[FileListPanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      val zipF = entriesF.map { entries =>
        val totalSize = entries.map(_.size).sum
        props.dispatch(FileListDiskSpaceUpdatedAction(totalSize))
        props.dispatch(FileListDirChangedAction(FileListDir.curr, FileListDir(
          path = rootPath,
          isRoot = false,
          items = entries.filter(_.parent.isEmpty).map(ZipApi.convertToFileListItem)
        )))
      }.andThen {
        case Failure(_) => 
          props.dispatch(FileListDirChangedAction(FileListDir.curr, FileListDir(
            path = rootPath,
            isRoot = false,
            items = Nil
          )))
      }
      
      props.dispatch(FileListTaskAction(FutureTask("Reading zip archive", zipF)))
      ()
    }, Nil)
    
    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "C-pageup" if props.state.currDir.path == rootPath =>
          onClose()
        case "enter" | "C-pagedown" if (
          props.state.currentItem.exists(i => i.isDir && i.name == FileListItem.up.name)
            && props.state.currDir.path == rootPath
          ) =>
          onClose()
        case _ =>
          processed = false
      }

      processed
    }

    <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))()
  }
}

object ZipPanel {

  private[zip] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
}
