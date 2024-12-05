package farjs.viewer

import farjs.file.FileEvent.onFileView
import farjs.file.FileViewHistory
import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.PanelStacks
import farjs.viewer.ViewerEvent._
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object ViewerPlugin extends FileListPlugin(js.Array(
  "f3", onViewerOpenLeft, onViewerOpenRight, onFileView
)) {

  private[viewer] final var fs: FS = scommons.nodejs.fs

  override def onKeyTrigger(key: String,
                            stacks: PanelStacks,
                            data: js.UndefOr[js.Dynamic] = js.undefined
                           ): js.Promise[js.UndefOr[ReactClass]] = new js.Promise[js.UndefOr[ReactClass]]((resolve, _) => {
    
    val res =
      if (key == onFileView) {
        data.toOption.map { d =>
          val history = d.asInstanceOf[FileViewHistory]
          val size = fs.lstatSync(history.path).size
          new ViewerPluginUi(history.path, size).apply()
        }
      }
      else {
        val stack = key match {
          case `onViewerOpenLeft` => stacks.left.stack
          case `onViewerOpenRight` => stacks.right.stack
          case _ => PanelStacks.active(stacks).stack
        }
        val stackItem = stack.peek[FileListState]
        stackItem.getData().toOption.flatMap { data =>
          val FileListData(_, actions, state) = data
          FileListState.currentItem(state).filter(_ != FileListItem.up).toOption match {
            case Some(item) if actions.api.isLocal && !item.isDir =>
              val filePath = path.join(state.currDir.path, item.name)
              val size = fs.lstatSync(filePath).size
              val ui = new ViewerPluginUi(filePath, size)
              Some(ui.apply())
            case Some(item) if state.selectedNames.nonEmpty || item.isDir =>
              Some(new ViewItemsPopup(data).apply())
            case _ => None
          }
        }
      }

    resolve(res match {
      case Some(r) => r
      case None => js.undefined
    })
  })
}
