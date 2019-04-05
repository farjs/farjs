package scommons.react

import io.github.shogowada.scalajs.reactjs.VirtualDOM._
import io.github.shogowada.statictags._

import scala.scalajs.js

package object blessed {

  type BlessedScreenConfig = blessed.raw.BlessedScreenConfig
  type BlessedScreen = blessed.raw.BlessedScreen
  type BlessedElement = blessed.raw.BlessedElement

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
    lazy val rbScrollable = BlessedBooleanAttributeSpec("scrollable")
    lazy val rbAlwaysScroll = BlessedBooleanAttributeSpec("alwaysScroll")
    lazy val rbScrollbar = BlessedBooleanAttributeSpec("scrollbar")
    lazy val rbShadow = BlessedBooleanAttributeSpec("shadow")
    
    lazy val rbOnPress = OnEvent0Attribute("onPress")
    lazy val rbOnClick = OnEvent0Attribute("onClick")
    lazy val rbOnResize = OnEvent0Attribute("onResize")
    lazy val rbOnMouseDown = OnEvent0Attribute("onMousedown")
    lazy val rbOnMouseUp = OnEvent0Attribute("onMouseup")
  }
}
