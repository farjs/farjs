package farjs.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.api.FileListDir
import farjs.filelist.stack._
import farjs.fs.popups.DrivePopup._
import farjs.fs.{FSDisk, MockFSService}
import farjs.ui.WithSizeProps
import farjs.ui.menu.MenuPopupProps
import farjs.ui.task.TaskAction
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class DrivePopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  DrivePopup.withSizeComp = "WithSize".asInstanceOf[ReactClass]
  DrivePopup.menuPopup = "MenuPopup".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class FsService {
    val readDisks = mockFunction[js.Promise[js.Array[FSDisk]]]

    val fsService = new MockFSService(
      readDisksMock = readDisks
    )
  }

  it should "call onChangeDir(curr panel path) when onSelect" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val currStack = new PanelStack(isActive = true, js.Array[PanelStackItem[_]](
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], js.undefined, currState)
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.plain := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val renderContent = inside(findComponents(renderer.root, withSizeComp)) {
        case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
      }
      val resultContent = createTestRenderer(renderContent).root
      val menuProps = inside(findComponents(resultContent, menuPopup)) {
        case List(c) => c.props.asInstanceOf[MenuPopupProps]
      }

      //then
      onChangeDir.expects("C:/test")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "call onChangeDir(other panel path) when onSelect" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = false)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val currStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], js.undefined, currState)
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("C:/test", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.plain := props)(), leftStack = otherStack, rightStack = currStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val renderContent = inside(findComponents(renderer.root, withSizeComp)) {
        case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
      }
      val resultContent = createTestRenderer(renderContent).root
      val menuProps = inside(findComponents(resultContent, menuPopup)) {
        case List(c) => c.props.asInstanceOf[MenuPopupProps]
      }

      //then
      onChangeDir.expects("C:/test")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "call onChangeDir(new root dir) when onSelect" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val onChangeDir = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir, onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    val currState = FileListState(currDir = FileListDir("/test", isRoot = false, js.Array()))
    val currStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch: js.Function1[js.Any, Unit], js.undefined, currState)
    ), updater = null)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, js.Array()))
    val otherStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, otherState)
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(DrivePopup())(^.plain := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val renderContent = inside(findComponents(renderer.root, withSizeComp)) {
        case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
      }
      val resultContent = createTestRenderer(renderContent).root
      val menuProps = inside(findComponents(resultContent, menuPopup)) {
        case List(c) => c.props.asInstanceOf[MenuPopupProps]
      }

      //then
      onChangeDir.expects("C:")
      
      //when
      menuProps.onSelect(0)
      
      Succeeded
    }
  }

  it should "call onClose when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val onClose = mockFunction[Unit]
    val props = DrivePopupProps(dispatch, onChangeDir = _ => (), onClose = onClose, showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("C:", size = 1.0, free = 2.0, "Test")
    )))
    val renderer = createTestRenderer(withContext(<(DrivePopup())(^.plain := props)()))
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val renderContent = inside(findComponents(renderer.root, withSizeComp)) {
        case List(comp) => comp.props.asInstanceOf[WithSizeProps].render(60, 20)
      }
      val resultContent = createTestRenderer(renderContent).root
      val menuProps = inside(findComponents(resultContent, menuPopup)) {
        case List(c) => c.props.asInstanceOf[MenuPopupProps]
      }

      //then
      onClose.expects()
      
      //when
      menuProps.onClose()
      
      Succeeded
    }
  }

  it should "render component on Windows" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.win32
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir = _ => (), onClose = () => (), showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )))

    //when
    val renderer = createTestRenderer(withContext(<(DrivePopup())(^.plain := props)()))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertDrivePopup(renderer.root, List(
        "  C: │SYSTEM         │149341 M│ 77912 M ",
        "  D: │DATA           │803867 M│336615 M ",
        "  E: │               │        │         "
      ))
    }
  }

  it should "render component on Mac OS/Linux" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val fsService = new FsService
    DrivePopup.platform = Platform.darwin
    DrivePopup.fsService = fsService.fsService
    val props = DrivePopupProps(dispatch, onChangeDir = _ => (), onClose = () => (), showOnLeft = true)

    var disksF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      disksF = action.asInstanceOf[TaskAction].task.result.toFuture
    }
    fsService.readDisks.expects().returning(js.Promise.resolve[js.Array[FSDisk]](js.Array(
      FSDisk("/", size = 156595318784.0, free = 81697124352.0, "/"),
      FSDisk("/Volumes/TestDrive", size = 842915639296.0, free = 352966430720.0, "TestDrive")
    )))
    val panelInput = js.Dynamic.literal(
      width = 50
    ).asInstanceOf[BlessedElement]

    //when
    val renderer = createTestRenderer(withContext(
      <(DrivePopup())(^.plain := props)(), panelInput = panelInput
    ))

    //then
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //then
      assertDrivePopup(renderer.root, List(
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
                          leftStack: PanelStack = new PanelStack(isActive = true, js.Array(), null),
                          rightStack: PanelStack = new PanelStack(isActive = false, js.Array(), null)
                         ): ReactElement = {

    WithStacksSpec.withContext(element, WithStacksData(leftStack, panelInput), WithStacksData(rightStack, null))
  }

  private def assertDrivePopup(result: TestInstance,
                               expectedItems: List[String],
                               expectedLeft: String = "0%+0"): Assertion = {
    
    val textWidth = expectedItems.maxBy(_.length).length
    val width = textWidth + 3 * 2

    assertComponents(result.children, List(
      <(withSizeComp)(^.assertPlain[WithSizeProps](inside(_) {
        case WithSizeProps(render) =>
          val content = createTestRenderer(render(60, 20)).root

          assertNativeComponent(content, <(menuPopup)(^.assertPlain[MenuPopupProps](inside(_) {
            case MenuPopupProps(title, items, getLeft, _, _) =>
              title shouldBe "Drive"
              items.toList shouldBe expectedItems
              getLeft(width) shouldBe expectedLeft
          }))())
      }))()
    ))
  }
}
