package farjs.filelist.history

import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class HistoryProviderSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useHistoryProvider" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        HistoryProvider.useHistoryProvider
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
        "Error: HistoryProvider.Context is not found." +
          "\nPlease, make sure you use HistoryProvider.Context.Provider in parent components"
      )
    )
  }
}

object HistoryProviderSpec {

  def withHistoryProvider(element: ReactElement,
                          historyProvider: HistoryProvider = new MockHistoryProvider
                         ): ReactElement = {

    <(HistoryProvider.Context.Provider)(^.contextValue := historyProvider)(
      element
    )
  }
}
