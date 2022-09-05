package farjs.app

import farjs.app.filelist.FileListBrowserController
import farjs.app.task.FarjsTaskController
import farjs.app.util.DevTool
import farjs.ui.theme.{Theme, XTerm256Theme}
import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import scommons.nodejs.{global, process}
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.WithPortals
import scommons.react.blessed.raw.{Blessed, ReactBlessed}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  private val g: js.Dynamic = global.asInstanceOf[js.Dynamic]

  @JSExport("start")
  def start(showDevTools: Boolean = false,
            currentScreen: js.UndefOr[BlessedScreen] = js.undefined,
            onExit: js.UndefOr[js.Function0[Unit]] = js.undefined): BlessedScreen = {

    val screen = currentScreen.getOrElse {
      val screen = Blessed.screen(new BlessedScreenConfig {
        override val autoPadding = true
        override val smartCSR = true
        override val cursorShape = "underline"
      })
      val screenObj = screen.asInstanceOf[js.Dynamic]
      screenObj.savedConsoleLog = g.console.log
      screenObj.savedConsoleError = g.console.error

      screen.key(js.Array("C-e"), { (_, _) =>
        // cleanup/unmount components
        screen.destroy()

        g.console.log = screenObj.savedConsoleLog
        g.console.error = screenObj.savedConsoleError
        onExit.toOption match {
          case Some(onExit) => onExit()
          case None => process.exit(0)
        }
      })
      screen
    }

    if (screen.terminal == TerminalName.`xterm-256color`) {
      Theme.current = XTerm256Theme
    }

    val store = Redux.createStore(FarjsStateReducer.reduce)
    
    val root = new FarjsRoot(
      withPortalsComp = new WithPortals(screen),
      fileListComp = FileListBrowserController(),
      taskController = FarjsTaskController(),
      initialDevTool = if (showDevTools) DevTool.Logs else DevTool.Hidden
    )
    
    ReactBlessed.createBlessedRenderer(Blessed)(
      <.Provider(^.store := store)(
        <(root()).empty
      ),
      screen
    )
    screen
  }
}
