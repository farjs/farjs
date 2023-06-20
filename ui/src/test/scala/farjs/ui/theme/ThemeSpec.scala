package farjs.ui.theme

import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class ThemeSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useTheme" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        Theme.useTheme
        <.>()()
      }
    }

    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = { _: js.Any =>
    }

    //when
    val result = testRender(<(TestErrorBoundary())()(
      <(wrapper()).empty
    ))

    //then
    js.Dynamic.global.console.error = savedConsoleError

    assertNativeComponent(result,
      <.div()(
        "Error: Theme.Context is not found." +
          "\nPlease, make sure you use Theme.Context.Provider in parent components"
      )
    )
  }
}

object ThemeSpec {

  def withThemeContext(element: ReactElement, theme: Theme = DefaultTheme): ReactElement = {
    <(Theme.Context.Provider)(^.contextValue := theme)(
      element
    )
  }
}
