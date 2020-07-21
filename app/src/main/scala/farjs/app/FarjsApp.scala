package farjs.app

import farjs.ui.LogPanel
import farjs.ui.filelist.FileListController
import farjs.ui.filelist.popups.FileListPopupsController
import farjs.ui.menu.BottomMenu
import io.github.shogowada.scalajs.reactjs.redux.ReactRedux._
import io.github.shogowada.scalajs.reactjs.redux.Redux
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.{Portal, WithPortals}
import scommons.react.blessed.raw.{Blessed, ReactBlessed}
import scommons.react.hooks._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarjsApp")
object FarjsApp {

  @JSExport("start")
  def start(): BlessedScreen = {
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
    val leftPanelController = new FileListController(actions, isRight = false)
    val rightPanelController = new FileListController(actions, isRight = true)
    val fileListPopupsController = new FileListPopupsController(actions)
    
    ReactBlessed.render(
      <.Provider(^.store := store)(
        <(FarjsAppRoot())(^.wrapped := FarjsAppRootProps(
          leftPanelController,
          rightPanelController,
          fileListPopupsController
        ))()
      ),
      screen
    )
    screen
  }

  case class FarjsAppRootProps(leftPanelController: FileListController,
                               rightPanelController: FileListController,
                               fileListPopupsController: FileListPopupsController)
  
  object FarjsAppMain extends FunctionComponent[FarjsAppRootProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped

      useContext(Portal.Context)
      
      <.>()(
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

        <.box(^.rbTop := "100%-1")(
          <(BottomMenu())()()
        )
      )
    }
  }
  
  object FarjsAppRoot extends FunctionComponent[FarjsAppRootProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped
      
      <.>()(
        <.box(
          ^.rbWidth := "70%"
        )(
          <(WithPortals())()(
            <.>()(
              Portal.create(
                <(FarjsAppMain())(^.wrapped := props)()
              ),
              <(props.fileListPopupsController()).empty,
              <(FarjsTaskController()).empty
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
