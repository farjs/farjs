package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersHistory._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class FSFoldersHistorySpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSFoldersHistory.fsFoldersPopup = mockUiComponent("FSFoldersPopup")

  it should "call onChangeDir when onAction" in {
    //given
    val onChangeDir = mockFunction[String, Unit]
    val props = getFSFoldersHistoryProps(showPopup = true, onChangeDir = onChangeDir)
    val result = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)()).root

    //then
    onChangeDir.expects(props.currDirPath)

    assertComponents(result.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, onAction, _) =>
          selected shouldBe 0
          items shouldBe List(props.currDirPath)

          //when
          onAction(0)
      }))()
    ))
  }

  it should "render popup component" in {
    //given
    val props = getFSFoldersHistoryProps(showPopup = true, "")
    
    //when & then
    val renderer = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)())
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, _, onClose) =>
          selected shouldBe 0
          items shouldBe Nil
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))

    //when & then
    TestRenderer.act { () =>
      renderer.update(<(FSFoldersHistory())(^.wrapped := props.copy(currDirPath = "dir 1"))())
    }
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, _, onClose) =>
          selected shouldBe 0
          items shouldBe List("dir 1")
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))

    //when & then
    TestRenderer.act { () =>
      renderer.update(<(FSFoldersHistory())(^.wrapped := props.copy(currDirPath = "dir 2"))())
    }
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, _, onClose) =>
          selected shouldBe 1
          items shouldBe List("dir 1", "dir 2")
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))

    //when & then
    TestRenderer.act { () =>
      renderer.update(<(FSFoldersHistory())(^.wrapped := props.copy(currDirPath = "dir 1"))())
    }
    assertComponents(renderer.root.children, List(
      <(fsFoldersPopup())(^.assertWrapped(inside(_) {
        case FSFoldersPopupProps(selected, items, _, onClose) =>
          selected shouldBe 1
          items shouldBe List("dir 2", "dir 1")
          onClose should be theSameInstanceAs props.onHidePopup
      }))()
    ))
  }

  it should "render empty component" in {
    //given
    val props = getFSFoldersHistoryProps(showPopup = false)
    
    //when
    val result = createTestRenderer(<(FSFoldersHistory())(^.wrapped := props)()).root

    //then
    result.children.toList should be (empty)
  }
  
  private def getFSFoldersHistoryProps(showPopup: Boolean,
                                       currDirPath: String = "test/dir",
                                       onChangeDir: String => Unit = _ => (),
                                       onHidePopup: () => Unit = () => ()): FSFoldersHistoryProps = {
    FSFoldersHistoryProps(
      showPopup = showPopup,
      currDirPath = currDirPath,
      onChangeDir = onChangeDir,
      onHidePopup = onHidePopup
    )
  }
}
