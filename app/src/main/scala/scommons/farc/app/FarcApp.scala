package scommons.farc.app

import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.farc.ui.filelist._
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.{Blessed, ReactBlessed}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "FarcApp")
object FarcApp {

  @JSExport("start")
  def start(): BlessedScreen = {
    val screen = Blessed.screen(new BlessedScreenConfig {
      override val autoPadding = true
      override val smartCSR = true
      override val title = "FARc"
    })

    screen.key(js.Array("C-c", "f10"), { (_, _) =>
      process.exit(0)
    })

    ReactBlessed.render(<(FarcAppRoot())()(), screen)
    screen
  }

  private val borderStyle = new BlessedStyle {
    override val bg = "blue"
    override val fg = "white"
  }

  private val textStyle = new BlessedStyle {
    override val bg = "blue"
    override val fg = "white"
    override val focus = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }
  }
  
  case class FilePanelProps(size: (Int, Int))
  
  object FilePanel extends FunctionComponent[FilePanelProps] {

    protected def render(compProps: Props): ReactElement = {
      val props = compProps.wrapped
      val (width, height) = props.size
      
      <.box(^.rbStyle := textStyle)(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps((width, height), borderStyle))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (1, 0),
          width = width - 2,
          text = "/current/folder",
          style = textStyle,
          focused = true
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (0, 2),
          length = width,
          lineCh = SingleBorder.horizontalCh,
          style = borderStyle,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),
        <(HorizontalLine())(^.wrapped := HorizontalLineProps(
          pos = (0, height - 4),
          length = width,
          lineCh = SingleBorder.horizontalCh,
          style = borderStyle,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),
        <.box(
          ^.rbWidth := width - 2,
          ^.rbHeight := height - 7,
          ^.rbLeft := 1,
          ^.rbTop := 3
        )(
          <(FileList())(^.wrapped := FileListProps(
            size = (width - 2, height - 7),
            columns = 3,
            items = (1 to 10000).toList.map { i =>
              i -> {
                if (i % 10 == 0) s"file $i tooo loooooooooooooooooooooooooooooooooooooong"
                else s"file $i"
              }
            }
          ))()
        ),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (1, height - 3),
          width = width - 2 - 12,
          text = "current.file",
          style = textStyle,
          padding = 0
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Right,
          pos = (1 + width - 2 - 12, height - 3),
          width = 12,
          text = "123456",
          style = textStyle,
          padding = 0
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (1, height - 1),
          width = (width - 2) / 2,
          text = "123 4567 890 (3)",
          style = textStyle
        ))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = ((width - 2) / 2, height - 1),
          width = (width - 2) / 2,
          text = "123 4567 890",
          style = textStyle
        ))()
      )
    }
  }
  
  object FarcAppRoot extends FunctionComponent[Unit] {

    protected def render(props: Props): ReactElement = {
      <.>()(
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
        )(
          <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
            <(FilePanel())(^.wrapped := FilePanelProps(size = (width, height)))()
          }))()
        ),

        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(LogPanel())()()
          //<(ColorPanel())()()
        ),

        <.box(^.rbTop := "100%-1")(
          <(BottomMenuBar())(^.wrapped := BottomMenuBarProps(
            onClick = { msg =>
              println(msg)
            }
          ))()
        )
      )
    }
  }
}
