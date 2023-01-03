package farjs.fs

import farjs.fs.popups._
import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class FSServicesSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useServices" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        FSServices.useServices
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
        "Error: FSServices.Context is not found." +
          "\nPlease, make sure you use FSServices.Context.Provider in parent components"
      )
    )
  }
}

object FSServicesSpec {

  def withServicesContext(element: ReactElement,
                          folderShortcuts: FolderShortcutsService = new MockFolderShortcutsService
                         ): ReactElement = {

    <(FSServices.Context.Provider)(^.contextValue := FSServices(
      folderShortcuts = folderShortcuts
    ))(
      element
    )
  }
}
