package farjs.viewer

import farjs.file.FileEvent.onFileView
import farjs.file.FileViewHistory
import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.WithPanelStacksProps
import farjs.viewer.ViewerEvent._
import scommons.nodejs._
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

object ViewerPlugin extends FileListPlugin {

  private[viewer] var fs: FS = scommons.nodejs.fs

  override val triggerKeys: js.Array[String] = js.Array(
    "f3", onViewerOpenLeft, onViewerOpenRight, onFileView
  )

  override def onKeyTrigger(key: String,
                            stacks: WithPanelStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined
                           ): Future[Option[ReactClass]] = Future {
    
    if (key == onFileView) {
      data.toOption.map { d =>
        val history = d.asInstanceOf[FileViewHistory]
        val size = fs.lstatSync(history.path).size
        new ViewerPluginUi(history.path, size).apply()
      }
    }
    else {
      val stack = key match {
        case `onViewerOpenLeft` => stacks.leftStack
        case `onViewerOpenRight` => stacks.rightStack
        case _ => stacks.activeStack
      }
      val stackItem = stack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state).flatMap { case ((dispatch, actions), state) =>
        val data = FileListData(dispatch, actions, state)
        state.currentItem.filter(_ != FileListItem.up) match {
          case Some(item) if actions.isLocalFS && !item.isDir =>
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
  }
}
