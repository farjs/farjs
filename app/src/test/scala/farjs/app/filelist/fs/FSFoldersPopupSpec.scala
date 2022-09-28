package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersPopup._
import farjs.ui.WithSizeProps
import farjs.ui.popup.{ModalContentProps, PopupProps}
import farjs.ui.theme.DefaultTheme
import scommons.react.test._

class FSFoldersPopupSpec extends TestSpec with TestRendererUtils {

  FSFoldersPopup.popupComp = mockUiComponent("Popup")
  FSFoldersPopup.modalContentComp = mockUiComponent("ModalContent")
  FSFoldersPopup.withSizeComp = mockUiComponent("WithSize")
  FSFoldersPopup.fsFoldersViewComp = mockUiComponent("FSFoldersView")

  it should "render component with empty list" in {
    //given
    val props = getFSFoldersPopupProps(items = Nil)
    
    //when
    val result = createTestRenderer(<(FSFoldersPopup())(^.wrapped := props)()).root

    //then
    assertFSFoldersPopup(result, props, (60, 20), (56, 14))
  }
  
  it should "render component with min size" in {
    //given
    val props = getFSFoldersPopupProps(items = List.fill(20)("item"))
    
    //when
    val result = createTestRenderer(<(FSFoldersPopup())(^.wrapped := props)()).root

    //then
    assertFSFoldersPopup(result, props, (55, 13), (56, 14))
  }
  
  it should "render component with max height" in {
    //given
    val props = getFSFoldersPopupProps(items = List.fill(20)("item"))
    
    //when
    val result = createTestRenderer(<(FSFoldersPopup())(^.wrapped := props)()).root

    //then
    assertFSFoldersPopup(result, props, (60, 20), (56, 16))
  }
  
  it should "render component with max width" in {
    //given
    val props = getFSFoldersPopupProps(items = List.fill(20)(
      "iteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
    ))
    
    //when
    val result = createTestRenderer(<(FSFoldersPopup())(^.wrapped := props)()).root

    //then
    assertFSFoldersPopup(result, props.copy(items = List.fill(20)(
      "ite...eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeem"
    )), (60, 20), (60, 16))
  }
  
  private def getFSFoldersPopupProps(items: List[String]): FSFoldersPopupProps = {
    FSFoldersPopupProps(
      selected = 1,
      items = items,
      onAction = _ => (),
      onClose = () => ()
    )
  }
  
  private def assertFSFoldersPopup(result: TestInstance,
                                   props: FSFoldersPopupProps,
                                   screenSize: (Int, Int),
                                   expectedSize: (Int, Int)): Unit = {

    val theme = DefaultTheme.popup.menu
    val (width, height) = screenSize
    val (expectedWidth, expectedHeight) = expectedSize
    val contentWidth = expectedWidth - 2 * (paddingHorizontal + 1)
    val contentHeight = expectedHeight - 2 * (paddingVertical + 1)
    
    assertComponents(result.children, List(
      <(popupComp())(^.assertWrapped(inside(_) {
        case PopupProps(onClose, closable, focusable, _, _) =>
          onClose should be theSameInstanceAs props.onClose
          closable shouldBe true
          focusable shouldBe true
      }))(
        <(withSizeComp())(^.assertPlain[WithSizeProps](inside(_) {
          case WithSizeProps(render) =>
            val content = createTestRenderer(render(width, height)).root
            
            assertTestComponent(content, modalContentComp)({
              case ModalContentProps(title, size, style, padding, left) =>
                title shouldBe "Folders history"
                size shouldBe (expectedWidth -> expectedHeight)
                style shouldBe theme
                padding shouldBe FSFoldersPopup.padding
                left shouldBe "center"
            }, inside(_) { case List(view) =>
              assertTestComponent(view, fsFoldersViewComp) {
                case FSFoldersViewProps(left, top, width, height, selected, items, style, onAction) =>
                  left shouldBe 1
                  top shouldBe 1
                  width shouldBe contentWidth
                  height shouldBe contentHeight
                  selected shouldBe props.selected
                  items shouldBe props.items
                  style shouldBe theme
                  onAction should be theSameInstanceAs props.onAction
              }
            })
        }))()
      )
    ))
  }
}
