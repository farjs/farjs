package scommons.react

import io.github.shogowada.scalajs.reactjs.VirtualDOM._
import io.github.shogowada.statictags._

import scala.scalajs.js

package object blessed {

  type BlessedScreenConfig = blessed.raw.BlessedScreenConfig
  type BlessedScreen = blessed.raw.BlessedScreen

  type BlessedStyle = blessed.raw.BlessedStyle
  type BlessedBorder = blessed.raw.BlessedBorder
  type BlessedBorderStyle = blessed.raw.BlessedBorderStyle
  
  implicit class BlessedVirtualDOMElements(elements: VirtualDOMElements) {
    lazy val box: ElementSpec = elements("box")
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

    type OnPress = js.Function0[Unit]
    case class OnPressEventAttribute(name: String) extends AttributeSpec {
      def :=(onEvent: OnPress): Attribute[OnPress] = Attribute(name, onEvent, AS_IS)
    }
  }

  implicit class BlessedVirtualDOMAttributes(attributes: VirtualDOMAttributes) {

    import BlessedVirtualDOMAttributes._

    lazy val rbStyle = BlessedStyleAttributeSpec("style")
    lazy val rbBorder = BlessedBorderAttributeSpec("border")
    lazy val top = IntegerAttributeSpec("top")
    lazy val left = IntegerAttributeSpec("left")
    
    lazy val onPress = OnPressEventAttribute("onPress")
  }
}
