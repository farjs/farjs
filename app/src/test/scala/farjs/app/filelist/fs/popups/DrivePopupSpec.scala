package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.DrivePopup._
import farjs.app.filelist.fs.{FSDisk, MockFSService}
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.FileListState
import farjs.filelist.api.FileListDir
import farjs.filelist.stack._
import farjs.ui.menu.MenuPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class DrivePopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  DrivePopup.menuPopup = mockUiComponent("MenuPopup")

  //noinspection TypeAnnotation
  class FsService {
    val readDisks = mockFunction[Future[List[FSDisk]]]

    val fsService = new MockFSService(
      readDisksMock = readDisks
    )
  }

  it should "call onChangeDir(curr panel path) when onSelect" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val currStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, Some(currState))
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.wrapped := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      onChangeDir.expects("C:/test")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "call onChangeDir(other panel path) when onSelect" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = false)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val currStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, Some(currState))
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.wrapped := props)(), leftStack = otherStack, rightStack = currStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      onChangeDir.expects("C:/test")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "call onChangeDir(new root dir) when onSelect" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("/test", isRoot = false, Nil))
    val currStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), None, Some(currState))
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.wrapped := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      onChangeDir.expects("C:")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "render component on Windows" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir = _ => (), onClose = () => (), showOnLeft = true)

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
    val renderer = createTestRenderer(withContext(<(DrivePopup())(^.wrapped := props)()))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertDrivePopup(renderer.root.children.head, props, List(
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
    DrivePopup.platform = Platform.darwin
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir = _ => (), onClose = () => (), showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[FileListTaskAction].task.future
    }
    fsService.readDisks.expects().returning(Future.successful(List(
      FSDisk("/", size = 156595318784.0, free = 81697124352.0, "/"),
      FSDisk("/Volumes/TestDrive", size = 842915639296.0, free = 352966430720.0, "TestDrive")
    )))
    val panelInput = js.Dynamic.literal(
      width = 50
    ).asInstanceOf[BlessedElement]

    //when
    val renderer = createTestRenderer(withContext(
      <(DrivePopup())(^.wrapped := props)(), panelInput = panelInput
    ))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertDrivePopup(renderer.root.children.head, props, List(
        " /              │149341 M│ 77912 M ",
        " TestDrive      │803867 M│336615 M "
      ), expectedLeft = "0%+4")
    }
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
                          leftStack: PanelStack = new PanelStack(isActive = true, Nil, null),
                          rightStack: PanelStack = new PanelStack(isActive = false, Nil, null)
                         ): ReactElement = {

    WithPanelStacksSpec.withContext(element, leftStack, rightStack, panelInput)
  }

  private def assertDrivePopup(result: TestInstance,
                               props: DrivePopupProps,
                               expectedItems: List[String],
                               expectedLeft: String = "0%+0"): Assertion = {
    
    val textWidth = expectedItems.maxBy(_.length).length
    val width = textWidth + 3 * 2

    assertTestComponent(result, menuPopup) {
      case MenuPopupProps(title, items, getLeft, _, onClose) =>
        title shouldBe "Drive"
        items shouldBe expectedItems
        getLeft(width) shouldBe expectedLeft
        onClose should be theSameInstanceAs props.onClose
    }
  }
}
