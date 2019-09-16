package farclone.app

import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import farclone.ui._
import farclone.ui.filelist._
import farclone.ui.filelist.popups.FileListPopupsController
import farclone.ui.menu._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.WithPortals
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

    screen.key(js.Array("C-c"), { (_, _) =>
      process.exit(0)
    })

    val store = Redux.createStore(FarcStateReducer.reduce)
    val actions = FarcActions
    val leftPanelController = new FileListController(actions, isRight = false)
    val rightPanelController = new FileListController(actions, isRight = true)
    
    ReactBlessed.render(
      <.Provider(^.store := store)(
        <(FarcAppRoot())(^.wrapped := FarcAppRootProps(
          leftPanelController,
          rightPanelController
        ))()
      ),
      screen
    )
    screen
  }

  case class FarcAppRootProps(leftPanelController: FileListController,
                              rightPanelController: FileListController)
  
  object FarcAppRoot extends FunctionComponent[FarcAppRootProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped
      
      <.>()(
        <.box(
          ^.rbWidth := "70%"
        )(
          <(WithPortals())()(
            <.box()(
              <.box(
                ^.rbWidth := "50%",
                ^.rbHeight := "100%-1"
              )(
                <(props.leftPanelController()).empty
              ),
              <.box(
                ^.rbWidth := "50%",
                ^.rbHeight := "100%-1",
                ^.rbLeft := "50%"
              )(
                <(props.rightPanelController()).empty
              ),
              <(FileListPopupsController()).empty,
              <(FarcTaskController()).empty,
    
              <.box(^.rbTop := "100%-1")(
                <(BottomMenu())()()
              )
            )
          )
        ),
        <.box(
          ^.rbWidth := "30%",
          ^.rbHeight := "100%",
          ^.rbLeft := "70%"
        )(
          <(LogPanel())()()
          //<(ColorPanel())()()
        )
      )
    }
  }
}
