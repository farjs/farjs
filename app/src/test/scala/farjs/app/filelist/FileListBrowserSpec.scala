package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class FileListBrowserSpec extends TestSpec with TestRendererUtils {
  
  private val leftPanelComp = "Left".asInstanceOf[ReactClass]
  private val rightPanelComp = "Right".asInstanceOf[ReactClass]

  FileListBrowser.bottomMenuComp = () => "BottomMenu".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val fileListBrowser = new FileListBrowser(leftPanelComp, rightPanelComp)

    //when
    val result = createTestRenderer(<(fileListBrowser())()()).root

    //then
    inside(result.children.toList) { case List(left, right, menu) =>
      assertNativeComponent(left,
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
        )(
          <(leftPanelComp).empty
        )
      )
      assertNativeComponent(right,
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(rightPanelComp).empty
        )
      )

      assertNativeComponent(menu,
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())()()
        )
      )
    }
  }
}
