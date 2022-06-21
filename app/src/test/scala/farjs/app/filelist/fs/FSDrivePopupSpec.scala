package farjs.app.filelist.fs

import farjs.app.filelist.MockFileListActions
import farjs.app.filelist.fs.FSDrivePopup._
import farjs.filelist.FileListActions.{FileListDirChangeAction, FileListTaskAction}
import farjs.filelist.FileListState
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.ui.menu.MenuPopupProps
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

  FSDrivePopup.menuPopup = mockUiComponent("MenuPopup")

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

  it should "dispatch FileListDirChangeAction(curr panel path) when onPress" in {
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

    val currState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState)
    )
    var currStackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(FSDrivePopup())(^.wrapped := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val action = FileListDirChangeAction(FutureTask("Changing Dir",
        Future.successful(FileListDir("/", isRoot = true, items = List.empty[FileListItem]))
      ))
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      actions.changeDir.expects(dispatch, None, "C:/test").returning(action)
      dispatch.expects(action)
      onClose.expects()
      
      //when
      menuProps.onSelect(0)
      
      //then
      currStackState shouldBe List(currFsItem)
    }
  }

  it should "dispatch FileListDirChangeAction(other panel path) when onPress" in {
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

    val currState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val currFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState)
    )
    var currStackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None),
      currFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("C:/test", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(FSDrivePopup())(^.wrapped := props)(), leftStack = otherStack, rightStack = currStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val action = FileListDirChangeAction(FutureTask("Changing Dir",
        Future.successful(FileListDir("/", isRoot = true, items = List.empty[FileListItem]))
      ))
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      actions.changeDir.expects(dispatch, None, "C:/test").returning(action)
      dispatch.expects(action)
      onClose.expects()
      
      //when
      menuProps.onSelect(0)
      
      //then
      currStackState shouldBe List(currFsItem)
    }
  }

  it should "dispatch FileListDirChangeAction(new root dir) when onPress" in {
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

    val currState = FileListState(currDir = FileListDir("/test", isRoot = false, Nil))
    val curFsItem = PanelStackItem(
      "fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(currState)
    )
    var currStackState: List[PanelStackItem[_]] = List(
      PanelStackItem("otherComp1".asInstanceOf[ReactClass], None, None, None),
      PanelStackItem("otherComp2".asInstanceOf[ReactClass], None, None, None),
      curFsItem
    )
    val currStack = new PanelStack(isActive = true, currStackState, { f =>
      currStackState = f(currStackState)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    val otherState = FileListState(currDir = FileListDir("/test2", isRoot = false, Nil))
    val otherStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], None, None, Some(otherState))
    ), updater = null)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(FSDrivePopup())(^.wrapped := props)(), leftStack = currStack, rightStack = otherStack)
    )
    renderer.root.children.isEmpty shouldBe true
    
    eventually {
      disksF should not be null
    }.flatMap(_ => disksF).map { _ =>
      //given
      val action = FileListDirChangeAction(FutureTask("Changing Dir",
        Future.successful(FileListDir("/", isRoot = true, items = List.empty[FileListItem]))
      ))
      val menuProps = findComponentProps(renderer.root, menuPopup)

      //then
      actions.changeDir.expects(dispatch, None, "C:").returning(action)
      dispatch.expects(action)
      onClose.expects()
      
      //when
      menuProps.onSelect(0)
      
      //then
      currStackState shouldBe List(curFsItem)
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

    <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(leftStack, rightStack))(
      <(PanelStack.Context.Provider)(^.contextValue := PanelStackProps(
        isRight = false,
        panelInput = panelInput,
        stack =
          if (leftStack.isActive) leftStack
          else rightStack
      ))(element)
    )
  }

  private def assertFSDrivePopup(result: TestInstance,
                                 props: FSDrivePopupProps,
                                 expectedItems: List[String]): Assertion = {
    
    val textWidth = expectedItems.maxBy(_.length).length
    val width = textWidth + 3 * 2

    assertTestComponent(result, menuPopup) {
      case MenuPopupProps(title, items, getLeft, _, onClose) =>
        title shouldBe "Drive"
        items shouldBe expectedItems
        getLeft(width) shouldBe "0%+0"
        onClose should be theSameInstanceAs props.onClose
    }
  }
}
