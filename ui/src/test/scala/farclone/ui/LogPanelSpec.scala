package farclone.ui

import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.TestRenderer
import scommons.react.test.util.TestRendererUtils

import scala.scalajs.js.Dynamic.{global => g}

class LogPanelSpec extends TestSpec with TestRendererUtils {

  it should "render component and redirect log output" in {
    //given
    val oldLog = g.console.log
    
    //when & then
    val renderer = createTestRenderer(<(LogPanel())()())

    g.console.log should not be oldLog
    assertNativeComponent(renderer.root.children(0),
      <.log(
        ^.rbAutoFocus := false,
        ^.rbMouse := true,
        ^.rbStyle := LogPanel.styles.container,
        ^.rbScrollbar := true,
        ^.rbScrollable := true,
        ^.rbAlwaysScroll := true,
        ^.content := ""
      )()
    )
    
    //when & then
    TestRenderer.act { () =>
      println("test message 1")
      println("test message 2")
    }
    assertNativeComponent(renderer.root.children(0),
      <.log(
        ^.rbMouse := true,
        ^.rbStyle := LogPanel.styles.container,
        ^.rbScrollbar := true,
        ^.rbScrollable := true,
        ^.rbAlwaysScroll := true,
        ^.content := "test message 1\ntest message 2\n"
      )()
    )
    
    //when & then
    TestRenderer.act { () =>
      renderer.unmount()
    }
    g.console.log shouldBe oldLog
  }
}
