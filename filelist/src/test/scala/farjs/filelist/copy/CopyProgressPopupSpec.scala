package farjs.filelist.copy

import farjs.filelist.copy.CopyProgressPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.PopupProps
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

class CopyProgressPopupSpec extends TestSpec with TestRendererUtils {

  CopyProgressPopup.popupComp = () => "Popup".asInstanceOf[ReactClass]
  CopyProgressPopup.doubleBorderComp = () => "DoubleBorder".asInstanceOf[ReactClass]
  CopyProgressPopup.textLineComp = () => "TextLine".asInstanceOf[ReactClass]
  CopyProgressPopup.horizontalLineComp = () => "HorizontalLine".asInstanceOf[ReactClass]

  it should "call onCancel when close popup" in {
    //given
    val onCancel = mockFunction[Unit]
    val props = getCopyProgressPopupProps(onCancel = onCancel)
    val comp = testRender(<(CopyProgressPopup())(^.wrapped := props)())
    val popup = findComponentProps(comp, popupComp)

    //then
    onCancel.expects()

    //when
    popup.onClose()
  }

  it should "render component" in {
    //given
    val props = getCopyProgressPopupProps()

    //when
    val result = testRender(<(CopyProgressPopup())(^.wrapped := props)())

    //then
    assertCopyProgressPopup(result, props, List("[ Copy ]" -> 0, "[ Cancel ]" -> 10))
  }

  it should "convert seconds to time when toTime" in {
    //when & then
    toTime(0) shouldBe "00:00:00"
    toTime(1) shouldBe "00:00:01"
    toTime(61) shouldBe "00:01:01"
    toTime(3601) shouldBe "01:00:01"
    toTime(3661) shouldBe "01:01:01"
    toTime(3662) shouldBe "01:01:02"
  }
  
  it should "convert bits per second to speed when toSpeed" in {
    //when & then
    toSpeed(0) shouldBe "0b"
    toSpeed(99000) shouldBe "99000b"
    toSpeed(100000) shouldBe "100Kb"
    toSpeed(99000000) shouldBe "99000Kb"
    toSpeed(100000000) shouldBe "100Mb"
    toSpeed(99000000000d) shouldBe "99000Mb"
    toSpeed(100000000000d) shouldBe "100Gb"
  }
  
  private def getCopyProgressPopupProps(item: String = "test item",
                                        to: String = "test to",
                                        itemPercent: Int = 1,
                                        total: Double = 2,
                                        totalPercent: Int = 3,
                                        timeSeconds: Int = 4,
                                        leftSeconds: Int = 5,
                                        bytesPerSecond: Double = 6,
                                        onCancel: () => Unit = () => ()): CopyProgressPopupProps = {
    CopyProgressPopupProps(
      item = item,
      to = to,
      itemPercent = itemPercent,
      total = total,
      totalPercent = totalPercent,
      timeSeconds = timeSeconds,
      leftSeconds = leftSeconds,
      bytesPerSecond = bytesPerSecond,
      onCancel = onCancel
    )
  }

  private def assertCopyProgressPopup(result: TestInstance,
                                      props: CopyProgressPopupProps,
                                      actions: List[(String, Int)]): Unit = {
    val (width, height) = (50, 13)
    val theme = Theme.current.popup.regular

    def assertComponents(border: TestInstance,
                         label: TestInstance,
                         item: TestInstance,
                         to: TestInstance,
                         sep1: TestInstance,
                         total: TestInstance,
                         sep2: TestInstance,
                         time: TestInstance,
                         speed: TestInstance,
                         button: TestInstance): Assertion = {

      assertTestComponent(border, doubleBorderComp) {
        case DoubleBorderProps(resSize, resStyle, pos, title) =>
          resSize shouldBe (width - 6) -> (height - 2)
          resStyle shouldBe theme
          pos shouldBe 3 -> 1
          title shouldBe Some("Copy")
      }

      assertNativeComponent(label,
        <.text(
          ^.rbLeft := 5,
          ^.rbTop := 2,
          ^.rbStyle := theme,
          ^.content :=
            """Copying the file
              |
              |to
              |""".stripMargin
        )()
      )
      assertTestComponent(item, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 5 -> 3
          resWidth shouldBe (width - 10)
          text shouldBe props.item
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }
      assertTestComponent(to, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 5 -> 5
          resWidth shouldBe (width - 10)
          text shouldBe props.to
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }

      assertTestComponent(sep1, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 5 -> 7
          resLength shouldBe (width - 10)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe None
          endCh shouldBe None
      }
      assertTestComponent(total, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Center
          pos shouldBe 5 -> 7
          resWidth shouldBe (width - 10)
          text shouldBe f"Total: ${props.total}%,.0f"
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 1
      }

      assertTestComponent(sep2, horizontalLineComp) {
        case HorizontalLineProps(pos, resLength, lineCh, resStyle, startCh, endCh) =>
          pos shouldBe 5 -> 9
          resLength shouldBe (width - 10)
          lineCh shouldBe SingleBorder.horizontalCh
          resStyle shouldBe theme
          startCh shouldBe None
          endCh shouldBe None
      }

      assertNativeComponent(time,
        <.text(
          ^.rbLeft := 5,
          ^.rbTop := 10,
          ^.rbStyle := theme,
          ^.content := s"Time: ${toTime(props.timeSeconds)} Left: ${toTime(props.leftSeconds)}"
        )()
      )
      assertTestComponent(speed, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextLine.Right
          pos shouldBe 35 -> 10
          resWidth shouldBe (width - 40)
          text shouldBe s"${toSpeed(props.bytesPerSecond * 8)}/s"
          resStyle shouldBe theme
          focused shouldBe false
          padding shouldBe 0
      }

      assertNativeComponent(button,
        <.button(^.rbWidth := 0, ^.rbHeight := 0)()
      )
    }

    assertTestComponent(result, popupComp)({ case PopupProps(_, resClosable, focusable, _) =>
      resClosable shouldBe true
      focusable shouldBe true
    }, inside(_) { case List(box) =>
      assertNativeComponent(box,
        <.box(
          ^.rbClickable := true,
          ^.rbAutoFocus := false,
          ^.rbWidth := width,
          ^.rbHeight := height,
          ^.rbTop := "center",
          ^.rbLeft := "center",
          ^.rbShadow := true,
          ^.rbStyle := theme
        )(), inside(_) {
          case List(border, label, item, to, sep1, total, sep2, time, speed, button) =>
            assertComponents(border, label, item, to, sep1, total, sep2, time, speed, button)
        }
      )
    })
  }
}
