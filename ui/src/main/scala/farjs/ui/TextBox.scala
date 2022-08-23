package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.Blessed
import scommons.react.hooks._

object TextBox extends FunctionComponent[TextBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput

  def renderText(style: BlessedStyle, text: String): String = {
    renderText(
      isBold = style.bold.getOrElse(false),
      fgColor = style.fg.getOrElse("white"),
      bgColor = style.bg.getOrElse("black"),
      text = text
    )
  }

  def renderText(isBold: Boolean, fgColor: String, bgColor: String, text: String): String = {
    if (text.isEmpty) text
    else {
      val bold = if (isBold) "{bold}" else ""
      s"$bold{$fgColor-fg}{$bgColor-bg}${Blessed.escape(text)}{/}"
    }
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (state, setState) = useStateUpdater(() => TextInputState())
    
    <(textInputComp())(^.wrapped := TextInputProps(
      left = props.left,
      top = props.top,
      width = props.width,
      value = props.value,
      state = state,
      stateUpdater = setState,
      onChange = props.onChange,
      onEnter = props.onEnter
    ))()
  }
}
