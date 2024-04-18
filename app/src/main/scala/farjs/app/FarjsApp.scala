package farjs.app

import farjs.app.filelist.{FileListModule, FileListRoot}
import farjs.app.raw.BetterSqlite3WebSQL
import farjs.domain.FarjsDBContext
import farjs.filelist.theme.FileListTheme
import farjs.fs.FSFileListActions
import farjs.ui.app._
import farjs.ui.portal.WithPortals
import farjs.ui.task.{TaskError, TaskManager}
import farjs.ui.tool.DevTool
import scommons.nodejs.{process, global => nodeGlobal}
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{Blessed, ReactBlessed}
import scommons.websql.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  private val g: js.Dynamic = nodeGlobal.asInstanceOf[js.Dynamic]
  
  private type BlessedRenderer = js.Function2[ReactElement, BlessedScreen, Unit]

  @JSExport("start")
  def start(showDevTools: Boolean = false,
            onReady: js.UndefOr[js.Function0[Unit]] = js.undefined,
            onExit: js.UndefOr[js.Function0[Unit]] = js.undefined): BlessedScreen = {

    def createRenderer(): BlessedRenderer = ReactBlessed.createBlessedRenderer(Blessed)

    val screen = {
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

    val screenObj = screen.asInstanceOf[js.Dynamic]
    val renderer = screenObj.savedRenderer.asInstanceOf[js.UndefOr[BlessedRenderer]]
      .getOrElse(createRenderer())

    //overwrite default error handler to handle scala/java exceptions
    TaskManager.errorHandler = TaskError.errorHandler

    renderer(
      <(AppRoot)(^.plain := AppRootProps(
        loadMainUi = { dispatch =>
          onReady.foreach(_.apply())

          prepareDB().map { db =>
            val ctx = new FarjsDBContext(db)
            val fileListModule = new FileListModule(ctx)
            val mainUi = new FileListRoot(dispatch.asInstanceOf[js.Function1[Any, Unit]], fileListModule, WithPortals.create(screen)).apply()
            val theme =
              if (screen.terminal == TerminalName.`xterm-256color`) FileListTheme.xterm256Theme
              else FileListTheme.defaultTheme

            LoadResult(theme, mainUi)
          }.toJSPromise
        },
        initialDevTool = if (showDevTools) DevTool.Logs else DevTool.Hidden,
        defaultTheme = FileListTheme.defaultTheme
      ))(),
      screen
    )
    screen
  }

  private def prepareDB(): Future[Database] = {
    val dbF = for {
      _ <- FSFileListActions.api.mkDirs(js.Array(FarjsData.getDataDir: _*)).toFuture
      db = BetterSqlite3WebSQL.openDatabase(FarjsData.getDBFilePath)
      _ <- FarjsDBMigrations.apply(db)
    } yield db

    dbF.recover {
      case error =>
        Console.err.println(s"Failed to prepare DB, error: $error")
        throw error
    }
  }
}
