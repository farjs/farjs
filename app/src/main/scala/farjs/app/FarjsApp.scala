package farjs.app

import farjs.app.filelist.FileListBrowser
import farjs.ui.filelist.FileListController
import farjs.ui.filelist.popups.FileListPopupsController
import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
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

    screen.key(js.Array("C-c"), { (_, _) =>
      process.exit(0)
    })

    val store = Redux.createStore(FarjsStateReducer.reduce)
    val actions = FarjsActions
    
    val fileListComp = new FileListBrowser(
      leftPanelComp = new FileListController(actions, isRight = false).apply(),
      rightPanelComp = new FileListController(actions, isRight = true).apply()
    ).apply()
    val root = new FarjsRoot(
      fileListComp = fileListComp,
      fileListPopups = new FileListPopupsController(actions).apply(),
      taskController = FarjsTaskController(),
      showDevTools = showDevTools
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
