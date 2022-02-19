package farjs.app

import farjs.app.filelist.FileListBrowserController
import farjs.app.task.FarjsTaskController
import farjs.app.util.DevTool
import farjs.ui.theme.{Theme, XTerm256Theme}
import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.WithPortals
import scommons.react.blessed.raw.{Blessed, BlessedOverrides, ReactBlessed}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  @JSExport("start")
  def start(showDevTools: Boolean = false): BlessedScreen = {
    BlessedOverrides.apply()
    
    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "FAR.js"
      override val cursorShape = "underline"
    })

    if (screen.terminal == TerminalName.`xterm-256color`) {
      Theme.current = XTerm256Theme
    }
    
    screen.key(js.Array("C-e"), { (_, _) =>
      process.exit(0)
    })

    val store = Redux.createStore(FarjsStateReducer.reduce)
    
    val root = new FarjsRoot(
      withPortalsComp = new WithPortals(screen),
      fileListComp = FileListBrowserController(),
      taskController = FarjsTaskController(),
      initialDevTool = if (showDevTools) DevTool.Logs else DevTool.Hidden
    )
    
    ReactBlessed.render(
      <.Provider(^.store := store)(
        <(root()).empty
      ),
      screen
    )
    screen
  }
}
