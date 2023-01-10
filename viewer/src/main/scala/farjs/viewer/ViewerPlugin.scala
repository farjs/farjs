package farjs.viewer

import farjs.filelist._
import farjs.filelist.api.FileListItem
import farjs.filelist.stack.WithPanelStacksProps
import scommons.nodejs._
import scommons.react._

object ViewerPlugin extends FileListPlugin {

  override val triggerKey: Option[String] = Some("f3")

  override def onKeyTrigger(stacks: WithPanelStacksProps): Option[ReactClass] = {
    val stackItem = stacks.activeStack.peek[FileListState]
    stackItem.getActions.zip(stackItem.state).flatMap { case ((dispatch, actions), state) =>
      state.currentItem.filter(_ != FileListItem.up) match {
        case Some(item) if actions.isLocalFS && !item.isDir =>
          val filePath = path.join(state.currDir.path, item.name)
          val ui = new ViewerPluginUi(filePath)
          Some(ui.apply())
        case _ => None
      }
    }
  }
}
