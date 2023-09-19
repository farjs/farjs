package farjs.viewer

import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.popup.StatusPopupProps
import farjs.viewer.ViewerSearch._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

class ViewerSearchSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  ViewerSearch.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]

  it should "render status popup" in {
    //given
    val onComplete = mockFunction[Unit]
    val props = getViewerSearchProps(searchTerm = "test", onComplete = onComplete)
    
    //when
    val result = createTestRenderer(withThemeContext(<(ViewerSearch())(^.wrapped := props)()))

    //then
    assertComponents(result.root.children, List(
      <(statusPopupComp)(^.assertPlain[StatusPopupProps](inside(_) {
        case StatusPopupProps(text, title, onClose) =>
          text shouldBe s"""Searching for\n"${props.searchTerm}""""
          title shouldBe "Search"
          
          //then
          onComplete.expects()
          
          //when
          onClose.foreach(_.apply())
      }))()
    ))
  }

  private def getViewerSearchProps(searchTerm: String,
                                   onComplete: () => Unit) = {
    ViewerSearchProps(
      searchTerm = searchTerm,
      onComplete = onComplete
    )
  }
}
