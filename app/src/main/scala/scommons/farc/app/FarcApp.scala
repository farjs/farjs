package scommons.farc.app

import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import scommons.farc.ui._
import scommons.farc.ui.filelist._
import scommons.farc.ui.menu._
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

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })

    val store = Redux.createStore(FarcStateReducer.reduce)
    val actions = FarcActions
    val leftPanelController = new FileListController(actions, isRight = false)
    val rightPanelController = new FileListController(actions, isRight = true)
    
    ReactBlessed.render(
      <.Provider(^.store := store)(
        <(WithPortals())()(
          <(FarcAppRoot())(^.wrapped := FarcAppRootProps(
            leftPanelController,
            rightPanelController
          ))()
        )
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
          ^.rbWidth := "35%",
          ^.rbHeight := "100%-1"
        )(
          <(props.leftPanelController()).empty
        ),
        <.box(
          ^.rbWidth := "35%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "35%"
        )(
          <(props.rightPanelController()).empty
        ),
        <.box(
          ^.rbWidth := "30%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "70%"
        )(
          <(LogPanel())()()
          //<(ColorPanel())()()
        ),

        <.box(^.rbTop := "100%-1")(
          <(BottomMenu())()()
        )
      )
    }
  }
}
