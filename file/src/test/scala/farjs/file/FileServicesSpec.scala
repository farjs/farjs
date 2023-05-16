package farjs.file

import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class FileServicesSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useServices" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        FileServices.useServices
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
        "Error: FileServices.Context is not found." +
          "\nPlease, make sure you use FileServices.Context.Provider in parent components"
      )
    )
  }
}

object FileServicesSpec {

  def withServicesContext(element: ReactElement,
                          fileViewHistory: FileViewHistoryService = new MockFileViewHistoryService
                         ): ReactElement = {

    <(FileServices.Context.Provider)(^.contextValue := FileServices(
      fileViewHistory = fileViewHistory
    ))(element)
  }
}
