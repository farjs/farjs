package farjs.viewer

import farjs.file.popups.TextSearchPopupProps
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.viewer.ViewerSearch._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class ViewerSearchSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerSearch.textSearchPopup = mockUiComponent("TextSearchPopup")

  it should "render search popup" in {
    //given
    val onHideSearchPopup = mockFunction[Unit]
    val props = getViewerSearchProps(showSearchPopup = true, onHideSearchPopup = onHideSearchPopup)
    
    //when
    val result = createTestRenderer(withThemeContext(<(ViewerSearch())(^.wrapped := props)()))

    //then
    assertComponents(result.root.children, List(
      <(textSearchPopup())(^.assertWrapped(inside(_) {
        case TextSearchPopupProps(_, onCancel) =>
          //then
          onHideSearchPopup.expects()
          
          //when
          onCancel()
      }))()
    ))
  }

  private def getViewerSearchProps(showSearchPopup: Boolean,
                                   onHideSearchPopup: () => Unit) = {
    ViewerSearchProps(
      showSearchPopup = showSearchPopup,
      onHideSearchPopup = onHideSearchPopup
    )
  }
}
