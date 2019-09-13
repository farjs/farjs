package farclone.ui

import scommons.react._
import scommons.react.blessed._

object ColorPanel extends FunctionComponent[Unit] {
  
  protected def render(props: Props): ReactElement = {
    
    def renderRow(pos: Int, bgColor: String, bgName: String, isBold: Boolean): ReactElement = {
      val colorsLine = colors.map { case (color, name, _) =>
        s"{$color-fg}{$bgColor-bg}$name{/}"
      }.mkString

      <.text(
        ^.key := s"$pos",
        ^.rbTop := pos,
        ^.rbTags := true,
        ^.rbStyle := new BlessedStyle {
          override val bold = isBold
        },
        ^.content := s"$bgName$colorsLine"
      )()
    }
    
    <.>()(
      colors.map { case (id, name, index) =>
        renderRow(index, id, name, isBold = false)
      },
      colors.map { case (id, name, index) =>
        renderRow(colors.size + index, id, name, isBold = true)
      }
    )
  }

  private lazy val colors = List(
    " black ",
    " red   ",
    " green ",
    " yellow",
    " blue  ",
    "magenta",
    " cyan  ",
    " white "
  ).zipWithIndex.map { case (name, index) => (name.trim, name, index) }
}
