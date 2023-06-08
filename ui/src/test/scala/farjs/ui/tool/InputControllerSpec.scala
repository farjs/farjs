package farjs.ui.tool

import farjs.ui.tool.InputController._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

class InputControllerSpec extends TestSpec with TestRendererUtils {

  InputController.logPanelComp = mockUiComponent("LogPanel")
  InputController.maxBufferLength = 10

  it should "render component and collect keys input" in {
    //given
    js.typeOf(g.farjsLogKeys) shouldBe "undefined"
    
    //when & then
    val renderer = createTestRenderer(<(InputController())()())
    js.typeOf(g.farjsLogKeys) should not be "undefined"

    //when & then
    g.farjsLogKeys("key 1")
    assertComponents(renderer.root.children, List(
      <(logPanelComp())(^.assertPlain[LogPanelProps](inside(_) {
        case LogPanelProps(content) => content shouldBe "key 1\n"
      }))()
    ))
    
    //when & then
    g.farjsLogKeys("k 2")
    assertComponents(renderer.root.children, List(
      <(logPanelComp())(^.assertPlain[LogPanelProps](inside(_) {
        case LogPanelProps(content) => content shouldBe "key 1\nk 2\n"
      }))()
    ))
    
    //when & then
    g.farjsLogKeys("k 3")
    assertComponents(renderer.root.children, List(
      <(logPanelComp())(^.assertPlain[LogPanelProps](inside(_) {
        case LogPanelProps(content) => content shouldBe "k 2\nk 3\n"
      }))()
    ))
    
    //when & then
    TestRenderer.act { () =>
      renderer.unmount()
    }
    js.typeOf(g.farjsLogKeys) shouldBe "undefined"
  }
}
