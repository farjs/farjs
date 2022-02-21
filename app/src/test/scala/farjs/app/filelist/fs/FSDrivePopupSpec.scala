package farjs.app.filelist.fs

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.fs.FSDrivePopup._
import farjs.filelist.FileListActions.{FileListDirChangeAction, FileListTaskAction}
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStack, PanelStackItem, PanelStackProps}
import farjs.ui._
import farjs.ui.popup.{ModalContentProps, PopupProps}
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class FSDrivePopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  FSDrivePopup.popupComp = mockUiComponent("Popup")
  FSDrivePopup.modalContentComp = mockUiComponent("ModalContent")
  FSDrivePopup.buttonComp = mockUiComponent("Button")

  //noinspection TypeAnnotation
  class Actions {
    val changeDir = mockFunction[Dispatch, Option[String], String, FileListDirChangeAction]

    val actions = new MockFileListActions(
      changeDirMock = changeDir
    )
  }

  //noinspection TypeAnnotation
  class FsService {
    val readDisks = mockFunction[Future[List[FSDisk]]]

    val fsService = new MockFSService(
      readDisksMock = readDisks
    )
  }

  it should "dispatch FileListDirChangeAction and call onClose when onPress item" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    FSDrivePopup.platform = Platform.win32
    FSDrivePopup.fsService = fsService.fsService
    val props = FSDrivePopupProps(dispatch, onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))
    val fsItem = PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), None)
    var stackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      fsItem
    )
    val stack = new PanelStack(isActive = true, stackState, { f =>
      stackState = f(stackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when & then
    val renderer = createTestRenderer(withContext(<(FSDrivePopup())(^.wrapped := props)(), stack = stack))
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      inside(findProps(renderer.root, buttonComp)) {
        case List(item1, _, _) =>
          //given
          val action = FileListDirChangeAction(FutureTask("Changing Dir",
            Future.successful(FileListDir("/", isRoot = true, items = List.empty[FileListItem]))
          ))

          //then
          actions.changeDir.expects(dispatch, None, "C:").returning(action)
          dispatch.expects(action)
          onClose.expects()
          
          //when
          item1.onPress()
          
          //then
          stackState shouldBe List(fsItem)
      }
    }
  }

  it should "render component on Windows" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSDrivePopup.platform = Platform.win32
    FSDrivePopup.fsService = fsService.fsService
    val props = FSDrivePopupProps(dispatch, onClose = () => (), showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    //when
    val renderer = createTestRenderer(withContext(<(FSDrivePopup())(^.wrapped := props)()))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertFSDrivePopup(renderer.root.children.head, props, List(
        "  C: │SYSTEM         │149341 M│ 77912 M ",
        "  D: │DATA           │803867 M│336615 M ",
        "  E: │               │        │         "
      ))
    }
  }

  it should "render component on Mac OS/Linux" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSDrivePopup.platform = Platform.darwin
    FSDrivePopup.fsService = fsService.fsService
    val props = FSDrivePopupProps(dispatch, onClose = () => (), showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("/", size = 156595318784.0, free = 81697124352.0, "/"),
      FSDisk("/Volumes/TestDrive", size = 842915639296.0, free = 352966430720.0, "TestDrive")
    )))

    //when
    val renderer = createTestRenderer(withContext(<(FSDrivePopup())(^.wrapped := props)()))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertFSDrivePopup(renderer.root.children.head, props, List(
        " /              │149341 M│ 77912 M ",
        " TestDrive      │803867 M│336615 M "
      ))
    }
  }

  it should "left pos when getLeftPos" in {
    //when & then
    getLeftPos(10, showOnLeft = true, 5) shouldBe "0%+2"
    getLeftPos(5, showOnLeft = true, 5) shouldBe "0%+0"
    getLeftPos(5, showOnLeft = true, 10) shouldBe "0%+0"
    getLeftPos(5, showOnLeft = false, 5) shouldBe "50%+0"
    getLeftPos(10, showOnLeft = false, 5) shouldBe "50%+2"
    getLeftPos(5, showOnLeft = false, 10) shouldBe "50%-5"
    getLeftPos(5, showOnLeft = false, 11) shouldBe "0%+0"
  }

  it should "convert bytes to compact form when toCompact" in {
    //when & then
    toCompact(0) shouldBe ""
    toCompact(1000d * 1024d) shouldBe "1024000"
    toCompact(1000d * 1024d + 1) shouldBe "1000 K"
    toCompact(1000d * 1024d * 1024d) shouldBe "1024000 K"
    toCompact(1000d * 1024d * 1024d + 1) shouldBe "1000 M"
    toCompact(1000d * 1024d * 1024d * 1024d) shouldBe "1024000 M"
    toCompact(1000d * 1024d * 1024d * 1024d + 1) shouldBe "1000 G"
  }

  private def withContext(element: ReactElement,
                          panelInput: BlessedElement = null,
                          stack: PanelStack = null): ReactElement = {

    <(PanelStack.Context.Provider)(^.contextValue := PanelStackProps(isRight = false, panelInput, stack))(
      element
    )
  }

  private def assertFSDrivePopup(result: TestInstance,
                                 props: FSDrivePopupProps,
                                 expectedItems: List[String]): Assertion = {
    
    val textWidth = expectedItems.maxBy(_.length).length
    val width = textWidth + 3 * 2
    val height = 2 * 2 + expectedItems.size
    val theme = Theme.current.popup.menu

    assertTestComponent(result, popupComp)({
      case PopupProps(onClose, closable, focusable, _) =>
        onClose should be theSameInstanceAs props.onClose
        closable shouldBe true
        focusable shouldBe true
    }, inside(_) { case List(content) =>
      var resSize = 0 -> 0
      assertTestComponent(content, modalContentComp)({
        case ModalContentProps(title, size, style, padding, left) =>
          title shouldBe "Drive"
          resSize = size
          style shouldBe theme
          padding shouldBe FSDrivePopup.padding
          left shouldBe "0%+0"
      }, inside(_) { case lines =>
        lines.size shouldBe expectedItems.size
        lines.zipWithIndex.zip(expectedItems).foreach { case ((line, index), expected) =>
          assertTestComponent(line, buttonComp) {
            case ButtonProps(pos, label, resStyle, _) =>
              pos shouldBe 1 -> (1 + index)
              label shouldBe expected
              resStyle shouldBe theme
          }
        }

        resSize shouldBe width -> height
      })
    })
  }
}
