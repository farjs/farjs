package farjs.app.util

import scommons.react.blessed._
import scommons.react.test._

class LogPanelSpec extends TestSpec with TestRendererUtils {

  it should "render component" in {
    //given
    val props = LogPanelProps("some log content")
    
    //when & then
    val result = testRender(<(LogPanel())(^.wrapped := props)())

    assertNativeComponent(result,
      <.log(
        ^.rbAutoFocus := false,
        ^.rbMouse := true,
        ^.rbStyle := LogPanel.styles.container,
        ^.rbScrollbar := true,
        ^.rbScrollable := true,
        ^.rbAlwaysScroll := true,
        ^.content := props.content
      )()
    )
  }
}
