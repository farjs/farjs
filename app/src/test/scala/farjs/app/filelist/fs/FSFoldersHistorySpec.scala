package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersHistory._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class FSFoldersHistorySpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSFoldersHistory.fsFoldersPopup = mockUiComponent("FSFoldersPopup")

  ignore should "handle onAction" in {
    //given
    val props = FSFoldersHistoryProps(showPopup = true, () => ())
    
    //when
    val result = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)()).root

    //then
    assertComponents(result.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, onAction, _) =>
          selected shouldBe 0
          items shouldBe Nil
          //TODO
      }))()
    ))
  }

  it should "render popup component" in {
    //given
    val props = FSFoldersHistoryProps(showPopup = true, () => ())
    
    //when
    val result = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)()).root

    //then
    assertComponents(result.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, _, onClose) =>
          selected shouldBe 0
          items shouldBe Nil
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))
  }

  it should "render empty component" in {
    //given
    val props = FSFoldersHistoryProps(showPopup = false, () => ())
    
    //when
    val result = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)()).root

    //then
    result.children.toList should be (empty)
  }
}
