package farjs.copymove

import farjs.copymove.CopyProgressPopup._
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup.ModalProps
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Assertion
import scommons.react.ReactClass
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class CopyProgressPopupSpec extends TestSpec with TestRendererUtils {

  CopyProgressPopup.modalComp = mockUiComponent("Modal")
  CopyProgressPopup.textLineComp = "TextLine".asInstanceOf[ReactClass]
  CopyProgressPopup.horizontalLineComp = mockUiComponent("HorizontalLine")

  it should "render component when copy" in {
    //given
    val props = getCopyProgressPopupProps(move = false)

    //when
    val result = testRender(withThemeContext(<(CopyProgressPopup())(^.wrapped := props)()))

    //then
    assertCopyProgressPopup(result, props)
  }

  it should "render component when move" in {
    //given
    val props = getCopyProgressPopupProps(move = true)

    //when
    val result = testRender(withThemeContext(<(CopyProgressPopup())(^.wrapped := props)()))

    //then
    assertCopyProgressPopup(result, props)
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
  
  private def getCopyProgressPopupProps(move: Boolean,
                                        item: String = "test item",
                                        to: String = "test to",
                                        itemPercent: Int = 1,
                                        total: Double = 2,
                                        totalPercent: Int = 3,
                                        timeSeconds: Int = 4,
                                        leftSeconds: Int = 5,
                                        bytesPerSecond: Double = 6,
                                        onCancel: () => Unit = () => ()): CopyProgressPopupProps = {
    CopyProgressPopupProps(
      move = move,
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

  private def assertCopyProgressPopup(result: TestInstance, props: CopyProgressPopupProps): Unit = {
    val (width, height) = (50, 13)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = DefaultTheme.popup.regular

    def assertComponents(label: TestInstance,
                         item: TestInstance,
                         to: TestInstance,
                         itemPercent: TestInstance,
                         sep1: TestInstance,
                         total: TestInstance,
                         totalPercent: TestInstance,
                         sep2: TestInstance,
                         time: TestInstance,
                         speed: TestInstance,
                         button: TestInstance): Assertion = {

      assertNativeComponent(label,
        <.text(
          ^.rbLeft := contentLeft,
          ^.rbTop := 1,
          ^.rbStyle := theme,
          ^.content :=
            s"""${if (props.move) "Moving" else "Copying"} the file
               |
               |to
               |""".stripMargin
        )()
      )
      assertNativeComponent(item, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe contentLeft
          top shouldBe 2
          resWidth shouldBe contentWidth
          text shouldBe props.item
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())
      assertNativeComponent(to, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe contentLeft
          top shouldBe 4
          resWidth shouldBe contentWidth
          text shouldBe props.to
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())

      assertTestComponent(itemPercent, progressBarComp, plain = true) {
        case ProgressBarProps(percent, left, top, resLength, resStyle) =>
          percent shouldBe props.itemPercent
          left shouldBe contentLeft
          top shouldBe 5
          resLength shouldBe contentWidth
          resStyle shouldBe theme
      }
      assertTestComponent(sep1, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe contentLeft
          resTop shouldBe 6
          resLength shouldBe contentWidth
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe theme
          startCh shouldBe js.undefined
          endCh shouldBe js.undefined
      }
      assertNativeComponent(total, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.center
          left shouldBe contentLeft
          top shouldBe 6
          resWidth shouldBe contentWidth
          text shouldBe f"Total: ${props.total}%,.0f"
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe js.undefined
      }))())
      assertTestComponent(totalPercent, progressBarComp, plain = true) {
        case ProgressBarProps(percent, left, top, resLength, resStyle) =>
          percent shouldBe props.totalPercent
          left shouldBe contentLeft
          top shouldBe 7
          resLength shouldBe contentWidth
          resStyle shouldBe theme
      }

      assertTestComponent(sep2, horizontalLineComp, plain = true) {
        case HorizontalLineProps(resLeft, resTop, resLength, lineCh, resStyle, startCh, endCh) =>
          resLeft shouldBe contentLeft
          resTop shouldBe 8
          resLength shouldBe contentWidth
          lineCh shouldBe SingleChars.horizontal
          resStyle shouldBe theme
          startCh shouldBe js.undefined
          endCh shouldBe js.undefined
      }

      assertNativeComponent(time,
        <.text(
          ^.rbLeft := contentLeft,
          ^.rbTop := 9,
          ^.rbStyle := theme,
          ^.content := s"Time: ${toTime(props.timeSeconds)} Left: ${toTime(props.leftSeconds)}"
        )()
      )
      assertNativeComponent(speed, <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, resStyle, focused, padding) =>
          align shouldBe TextAlign.right
          left shouldBe (contentLeft + 30)
          top shouldBe 9
          resWidth shouldBe (contentWidth - 30)
          text shouldBe s"${toSpeed(props.bytesPerSecond * 8)}/s"
          resStyle shouldBe theme
          focused shouldBe js.undefined
          padding shouldBe 0
      }))())

      assertNativeComponent(button,
        <.button(^.rbWidth := 0, ^.rbHeight := 0)()
      )
    }

    assertTestComponent(result, modalComp)({ case ModalProps(title, size, resStyle, onCancel) =>
      title shouldBe (if (props.move) "Move" else "Copy")
      size shouldBe width -> height
      resStyle shouldBe theme
      onCancel should be theSameInstanceAs props.onCancel
    }, inside(_) {
      case List(label, item, to, itemPercent, sep1, total, totalPercent, sep2, time, speed, button) =>
        assertComponents(label, item, to, itemPercent, sep1, total, totalPercent, sep2, time, speed, button)
    })
  }
}
