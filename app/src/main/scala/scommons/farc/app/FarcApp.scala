package scommons.farc.app

import scommons.farc.api.filelist.FileListApi
import scommons.farc.app.filelist.FileListApiImpl
import scommons.farc.ui._
import scommons.farc.ui.filelist._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{Blessed, ReactBlessed}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarcApp")
object FarcApp {

  @JSExport("start")
  def start(): BlessedScreen = {
    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "FARc"
    })

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })

    val api = new FileListApiImpl
    
    ReactBlessed.render(<(FarcAppRoot())(^.wrapped := FarcAppRootProps(api))(), screen)
    screen
  }

  case class FarcAppRootProps(fileListApi: FileListApi)
  
  object FarcAppRoot extends FunctionComponent[FarcAppRootProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped
      
      <.>()(
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
        )(
          <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
            <(FilePanel())(^.wrapped := FilePanelProps(props.fileListApi, size = (width, height)))()
          }))()
        ),

        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(LogPanel())()()
          //<(ColorPanel())()()
        ),

        <.box(^.rbTop := "100%-1")(
          <(BottomMenuBar())(^.wrapped := BottomMenuBarProps(
            onClick = { msg =>
              println(msg)
            }
          ))()
        )
      )
    }
  }
}
