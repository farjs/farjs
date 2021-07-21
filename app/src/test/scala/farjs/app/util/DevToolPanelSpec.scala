package farjs.app.util

import farjs.app.util.DevToolPanel._
import farjs.ui.TextBox
import farjs.ui.theme.Theme
import org.scalatest.{Assertion, Succeeded}
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class DevToolPanelSpec extends TestSpec with TestRendererUtils {

  DevToolPanel.logPanelComp = () => "LogPanel".asInstanceOf[ReactClass]
  DevToolPanel.inputController = () => "InputController".asInstanceOf[ReactClass]
  DevToolPanel.colorPanelComp = () => "ColorPanel".asInstanceOf[ReactClass]

  it should "call onActivate when click on tab" in {
    //given
    val onActivate = mockFunction[DevTool, Unit]
    val props = DevToolPanelProps(DevTool.Colors, "test logs", onActivate)
    val comp = createTestRenderer(<(DevToolPanel())(^.wrapped := props)()).root
    val tab1 = inside(findComponents(comp, <.text.name)) {
      case List(t1, _, _) => t1
    }

    //then
    onActivate.expects(DevTool.Logs)
    
    //when
    tab1.props.onClick(null)
  }

  it should "render Logs component" in {
    //given
    val props = DevToolPanelProps(DevTool.Logs, "test logs", _ => ())

    //when
    val result = createTestRenderer(<(DevToolPanel())(^.wrapped := props)()).root

    //then
    assertDevToolPanel(
      result = result,
      props = props,
      expectedTabs = List(" Logs " -> 0, " Inputs " -> 6, " Colors " -> 14),
      activeTab = " Logs "
    )
  }
  
  it should "render Inputs component" in {
    //given
    val props = DevToolPanelProps(DevTool.Inputs, "test logs", _ => ())

    //when
    val result = createTestRenderer(<(DevToolPanel())(^.wrapped := props)()).root

    //then
    assertDevToolPanel(
      result = result,
      props = props,
      expectedTabs = List(" Logs " -> 0, " Inputs " -> 6, " Colors " -> 14),
      activeTab = " Inputs "
    )
  }
  
  it should "render Colors component" in {
    //given
    val props = DevToolPanelProps(DevTool.Colors, "test logs", _ => ())

    //when
    val result = createTestRenderer(<(DevToolPanel())(^.wrapped := props)()).root

    //then
    assertDevToolPanel(
      result = result,
      props = props,
      expectedTabs = List(" Logs " -> 0, " Inputs " -> 6, " Colors " -> 14),
      activeTab = " Colors "
    )
  }
  
  private def assertDevToolPanel(result: TestInstance,
                                 props: DevToolPanelProps,
                                 expectedTabs: List[(String, Int)],
                                 activeTab: String): Assertion = {

    val theme = Theme.current.popup.menu
    
    val (header, compWrap) = inside(result.children.toList) {
      case List(header, compWrap) => (header, compWrap)
    }

    assertNativeComponent(header, <.box(
      ^.rbWidth := "100%",
      ^.rbHeight := 1,
      ^.rbStyle := theme
    )(), inside(_) { case List(tabsWrap) =>
      assertNativeComponent(tabsWrap, <.box(
        ^.rbWidth := expectedTabs.map(_._1.length).sum,
        ^.rbHeight := 1,
        ^.rbLeft := "center"
      )(), { tabs =>
        tabs.size shouldBe expectedTabs.size
        tabs.zip(expectedTabs).foreach { case (tab, (label, pos)) =>
          assertNativeComponent(tab, <.text(
            ^.key := s"$pos",
            ^.rbAutoFocus := false,
            ^.rbClickable := true,
            ^.rbTags := true,
            ^.rbMouse := true,
            ^.rbLeft := pos,
            ^.content := {
              val style =
                if (label == activeTab) theme.focus.getOrElse(theme)
                else theme

              TextBox.renderText(style, label)
            }
          )())
        }
        Succeeded
      })
    })
    
    assertNativeComponent(compWrap, <.box(^.rbTop := 1)(), inside(_) { case List(comp) =>
      props.devTool match {
        case DevTool.Hidden => fail("unexpected dev tool")
        case DevTool.Logs =>
          assertTestComponent(comp, logPanelComp) { case LogPanelProps(resContent) =>
            resContent shouldBe props.logContent
          }
        case DevTool.Inputs =>
          assertNativeComponent(comp, <(inputController())()())
        case DevTool.Colors =>
          assertNativeComponent(comp, <(colorPanelComp())()())
      }
    })
  }
}
