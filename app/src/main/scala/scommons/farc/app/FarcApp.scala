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

  object FarcAppRoot extends FunctionComponent[Unit] {

    protected def render(props: Props): ReactElement = {
      <.>()(
        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1"
        )(
          <(WithSize())(^.wrapped := WithSizeProps({ (width, height) =>
            <.>()(
              <(DoubleBorder())(^.wrapped := DoubleBorderProps((width, height), borderStyle))(),
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
              <.text(
                ^.rbWidth := width - 2,
                ^.rbHeight := 1,
                ^.rbLeft := 1,
                ^.rbTop := 1,
                ^.rbStyle := textStyle,
                ^.content := "n /current/folder"
              )(),
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
              <.text(
                ^.rbWidth := width - 2,
                ^.rbHeight := 2,
                ^.rbLeft := 1,
                ^.rbTop := height - 3,
                ^.rbStyle := textStyle,
                ^.content := "info"
              )()
            )
          }))()
        ),

        <.box(
          ^.rbWidth := "50%",
          ^.rbHeight := "100%-1",
          ^.rbLeft := "50%"
        )(
          <(LogPanel())()()
//          <(ColorPanel())()()
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

    private val borderStyle = new BlessedStyle {
      override val bg = "blue"
      override val fg = "white"
    }
    
    private val textStyle = new BlessedStyle {
      override val bg = "blue"
      override val fg = "white"
    }
  }
}
