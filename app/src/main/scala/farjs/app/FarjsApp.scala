package farjs.app

import farjs.app.filelist.{FileListModule, FileListRoot}
import farjs.app.util.DevTool
import farjs.domain.FarjsDBContext
import farjs.fs.FSFileListActions
import farjs.ui.theme.{Theme, XTerm256Theme}
import scommons.nodejs.{process, global => nodeGlobal}
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.WithPortals
import scommons.react.blessed.raw.{Blessed, ReactBlessed}
import scommons.websql.{Database, WebSQL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  private val g: js.Dynamic = nodeGlobal.asInstanceOf[js.Dynamic]
  
  private type BlessedRenderer = js.Function2[ReactElement, BlessedScreen, Unit]

  @JSExport("start")
  def start(showDevTools: Boolean = false,
            currentScreen: js.UndefOr[BlessedScreen] = js.undefined,
            onExit: js.UndefOr[js.Function0[Unit]] = js.undefined): BlessedScreen = {

    def createRenderer(): BlessedRenderer = ReactBlessed.createBlessedRenderer(Blessed)

    val screen = currentScreen.getOrElse {
      val screen = Blessed.screen(new BlessedScreenConfig {
        override val autoPadding = true
        override val smartCSR = true
        override val tabSize = 1
        override val fullUnicode = true
        override val cursorShape = "underline"
      })
      val screenObj = screen.asInstanceOf[js.Dynamic]
      screenObj.savedRenderer = createRenderer()
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

    val root = new FarjsRoot(
      withPortalsComp = new WithPortals(screen),
      loadFileListUi = { dispatch =>
        prepareDB().map { db =>
          val ctx = new FarjsDBContext(db)
          val fileListModule = new FileListModule(ctx)
          new FileListRoot(dispatch, fileListModule).apply()
        }
      },
      initialDevTool = if (showDevTools) DevTool.Logs else DevTool.Hidden
    )
    
    val screenObj = screen.asInstanceOf[js.Dynamic]
    val renderer = screenObj.savedRenderer.asInstanceOf[js.UndefOr[BlessedRenderer]]
      .getOrElse(createRenderer())

    renderer(
      <(root()).empty,
      screen
    )
    screen
  }

  private def prepareDB(): Future[Database] = {
    val dbF = for {
      _ <- FSFileListActions.mkDirs(FarjsData.getDataDir)
      db = WebSQL.openDatabase(FarjsData.getDBFilePath)
      _ <- FarjsDBMigrations.apply(db)
    } yield db

    dbF.recover {
      case error =>
        Console.err.println(s"Failed to prepare DB, error: $error")
        throw error
    }
  }
}
