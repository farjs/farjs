package farjs.filelist

import farjs.filelist.history._
import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class FileListServicesSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useServices" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        FileListServices.useServices
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
        "Error: FileListServices.Context is not found." +
          "\nPlease, make sure you use FileListServices.Context.Provider in parent components"
      )
    )
  }
}

object FileListServicesSpec {

  def withServicesContext(element: ReactElement,
                          foldersHistory: FileListHistoryService = new MockFileListHistoryService,
                          mkDirsHistory: FileListHistoryService = new MockFileListHistoryService,
                          selectPatternsHistory: FileListHistoryService = new MockFileListHistoryService,
                          copyItemsHistory: FileListHistoryService = new MockFileListHistoryService
                         ): ReactElement = {

    <(FileListServices.Context.Provider)(^.contextValue := FileListServices(
      foldersHistory = foldersHistory,
      mkDirsHistory = mkDirsHistory,
      selectPatternsHistory = selectPatternsHistory,
      copyItemsHistory = copyItemsHistory
    ))(
      element
    )
  }
}
