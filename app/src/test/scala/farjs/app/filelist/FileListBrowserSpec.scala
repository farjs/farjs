package farjs.app.filelist

import farjs.ui.menu.BottomMenu
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class FileListBrowserSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val leftPanelComp = new FunctionComponent[Unit] {
      override protected def render(props: Props): ReactElement = {
        <.>()("Left")
      }
    }.apply()
    val rightPanelComp = new FunctionComponent[Unit] {
      override protected def render(props: Props): ReactElement = {
        <.>()("Right")
      }
    }.apply()
    val fileListBrowser = new FileListBrowser(leftPanelComp, rightPanelComp)

    //when
    val result = shallowRender(<(fileListBrowser())()())

    //then
    assertNativeComponent(result,
      <.>()(
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
        )(
          <(leftPanelComp).empty
        ),
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(rightPanelComp).empty
        ),

        <.box(^.rbTop := "100%-1")(
          <(BottomMenu())()()
        )
      )
    )
  }
}
