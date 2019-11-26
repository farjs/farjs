package farclone.app

import farclone.ui._
import farclone.ui.filelist._
import farclone.ui.filelist.popups.FileListPopupsController
import farclone.ui.menu._
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
    val fileListPopupsController = new FileListPopupsController(actions)
    
    ReactBlessed.render(
      <.Provider(^.store := store)(
        <(FarcAppRoot())(^.wrapped := FarcAppRootProps(
          leftPanelController,
          rightPanelController,
          fileListPopupsController
        ))()
      ),
      screen
    )
    screen
  }

  case class FarcAppRootProps(leftPanelController: FileListController,
                              rightPanelController: FileListController,
                              fileListPopupsController: FileListPopupsController)
  
  object FarcAppMain extends FunctionComponent[FarcAppRootProps] {

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
  
  object FarcAppRoot extends FunctionComponent[FarcAppRootProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped
      
      <.>()(
        <.box(
          ^.rbWidth := "70%"
        )(
          <(WithPortals())()(
            <.>()(
              Portal.create(
                <(FarcAppMain())(^.wrapped := props)()
              ),
              <(props.fileListPopupsController()).empty,
              <(FarcTaskController()).empty
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
