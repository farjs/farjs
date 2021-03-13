package scommons.react

import io.github.shogowada.scalajs.reactjs.VirtualDOM._
import io.github.shogowada.statictags._

import scala.scalajs.js

package object blessed {

  type BlessedEventEmitter = blessed.raw.BlessedEventEmitter
  type BlessedScreenConfig = blessed.raw.BlessedScreenConfig
  type BlessedScreen = blessed.raw.BlessedScreen
  type BlessedElement = blessed.raw.BlessedElement
  type MouseData = blessed.raw.MouseData
  type KeyboardKey = blessed.raw.KeyboardKey

  type BlessedStyle = blessed.raw.BlessedStyle
  type BlessedBorder = blessed.raw.BlessedBorder
  type BlessedBorderStyle = blessed.raw.BlessedBorderStyle
  type BlessedScrollBarStyle = blessed.raw.BlessedScrollBarStyle
  
  implicit class BlessedVirtualDOMElements(elements: VirtualDOMElements) {
    lazy val box: ElementSpec = elements("box")
    lazy val text: ElementSpec = elements("text")
    lazy val log: ElementSpec = elements("log")
  }

  object BlessedVirtualDOMAttributes {

    import VirtualDOMAttributes.Type._

    case class BlessedStyleAttributeSpec(name: String) extends AttributeSpec {
      def :=(value: js.UndefOr[BlessedStyle]): Attribute[BlessedStyle] = Attribute(name, value.orNull, AS_IS)
      def :=(value: BlessedStyle): Attribute[BlessedStyle] = Attribute(name, value, AS_IS)
      def :=(value: js.Array[BlessedStyle]): Attribute[js.Array[BlessedStyle]] = Attribute(name, value, AS_IS)
    }

    case class BlessedBorderAttributeSpec(name: String) extends AttributeSpec {
      def :=(value: BlessedBorder): Attribute[BlessedBorder] = Attribute(name, value, AS_IS)
    }

    case class BlessedPositionAttributeSpec(name: String) extends AttributeSpec {
      def :=(value: String): Attribute[String] = Attribute(name, value, AS_IS)
      def :=(value: Int): Attribute[Int] = Attribute(name, value, AS_IS)
    }

    case class BlessedBooleanAttributeSpec(name: String) extends AttributeSpec {
      def :=(value: Boolean): Attribute[Boolean] = Attribute(name, value, AS_IS)
    }

    type OnEvent0 = js.Function0[Unit]
    case class OnEvent0Attribute(name: String) extends AttributeSpec {
      def :=(onEvent: OnEvent0): Attribute[OnEvent0] = Attribute(name, onEvent, AS_IS)
    }
    
    type OnMouseEvent = js.Function1[MouseData, Unit]
    case class OnMouseEventAttribute(name: String) extends AttributeSpec {
      def :=(value: OnMouseEvent): Attribute[OnMouseEvent] = Attribute(name, value, AS_IS)
    }
    
    type OnKeypress = js.Function2[js.Dynamic, KeyboardKey, Unit]
    case class OnKeypressAttribute(name: String) extends AttributeSpec {
      def :=(onEvent: OnKeypress): Attribute[OnKeypress] = Attribute(name, onEvent, AS_IS)
    }
  }

  implicit class BlessedVirtualDOMAttributes(attributes: VirtualDOMAttributes) {

    import BlessedVirtualDOMAttributes._

    lazy val rbStyle = BlessedStyleAttributeSpec("style")
    lazy val rbBorder = BlessedBorderAttributeSpec("border")
    lazy val rbHeight = BlessedPositionAttributeSpec("height")
    lazy val rbWidth = BlessedPositionAttributeSpec("width")
    lazy val rbTop = BlessedPositionAttributeSpec("top")
    lazy val rbLeft = BlessedPositionAttributeSpec("left")
    lazy val rbMouse = BlessedBooleanAttributeSpec("mouse")
    lazy val rbTags = BlessedBooleanAttributeSpec("tags")
    lazy val rbClickable = BlessedBooleanAttributeSpec("clickable")
    lazy val rbKeyable = BlessedBooleanAttributeSpec("keyable")
    lazy val rbAutoFocus = BlessedBooleanAttributeSpec("autoFocus")
    lazy val rbScrollable = BlessedBooleanAttributeSpec("scrollable")
    lazy val rbAlwaysScroll = BlessedBooleanAttributeSpec("alwaysScroll")
    lazy val rbScrollbar = BlessedBooleanAttributeSpec("scrollbar")
    lazy val rbShadow = BlessedBooleanAttributeSpec("shadow")
    
    lazy val rbOnKeypress = OnKeypressAttribute("onKeypress")
    lazy val rbOnPress = OnEvent0Attribute("onPress")
    lazy val rbOnFocus = OnEvent0Attribute("onFocus")
    lazy val rbOnBlur = OnEvent0Attribute("onBlur")
    lazy val rbOnClick = OnMouseEventAttribute("onClick")
    lazy val rbOnWheeldown = OnMouseEventAttribute("onWheeldown")
    lazy val rbOnWheelup = OnMouseEventAttribute("onWheelup")
    lazy val rbOnResize = OnEvent0Attribute("onResize")
    lazy val rbOnMousedown = OnMouseEventAttribute("onMousedown")
    lazy val rbOnMouseup = OnMouseEventAttribute("onMouseup")
  }
}
