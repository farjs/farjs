package farjs.app

import farjs.ui.LogPanel
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal._
import scommons.react.test._

class FarjsRootSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val fileListComp = new FunctionComponent[Unit] {
      override protected def render(props: Props): ReactElement = {
        <.>()("FileList Comp")
      }
    }.apply()
    val fileListPopups = new FunctionComponent[Unit] {
      override protected def render(props: Props): ReactElement = {
        <.>()("FileListPopups Comp")
      }
    }.apply()
    val root = new FarjsRoot(fileListComp, fileListPopups)

    //when
    val result = shallowRender(<(root())()())

    //then
    assertNativeComponent(result,
      <.>()(
        <.box(
          ^.rbWidth := "70%"
        )(
          <(WithPortals())()(
            <.>()(
              Portal.create(
                <(fileListComp).empty
              ),
              <(fileListPopups).empty,
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
    )
  }
}
