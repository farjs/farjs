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

  it should "render component" in {
    //given
    val props = getFSFoldersPopupProps()
    
    //when
    val result = createTestRenderer(<(FSFoldersPopup())(^.wrapped := props)()).root

    //then
    assertFSFoldersPopup(result, props)
  }
  
  private def getFSFoldersPopupProps(selected: Int = 0,
                                     items: List[String] = List(
                                       "item 1",
                                       "item 2"
                                     )): FSFoldersPopupProps = {
    FSFoldersPopupProps(
      selected = selected,
      items = items,
      onAction = _ => (),
      onClose = () => ()
    )
  }
  
  private def assertFSFoldersPopup(result: TestInstance, props: FSFoldersPopupProps): Unit = {
    val theme = DefaultTheme.popup.menu
    val width = 75
    val height = 35
    val modalHeight = height - 4
    val contentWidth = width - paddingHorizontal * 2 - 2
    val contentHeight = modalHeight - paddingVertical * 2 - 2
    
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
                size shouldBe (width -> modalHeight)
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
