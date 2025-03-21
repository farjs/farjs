package farjs.copymove

import farjs.copymove.CopyMoveUi._
import farjs.copymove.CopyMoveUiAction._
import farjs.copymove.CopyMoveUiSpec._
import farjs.filelist.FileListActions._
import farjs.filelist.FileListActionsSpec.{assertFileListItemCreatedAction, assertFileListParamsChangedAction}
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem, MockFileListApi}
import farjs.filelist.history._
import farjs.ui.Dispatch
import farjs.ui.popup.MessageBoxProps
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Succeeded
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.Future
import scala.scalajs.js

class CopyMoveUiSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyMoveUi.copyItemsStats = mockUiComponent("CopyItemsStats")
  CopyMoveUi.copyItemsPopup = mockUiComponent("CopyItemsPopup")
  CopyMoveUi.copyProcessComp = mockUiComponent("CopyProcess")
  CopyMoveUi.messageBoxComp = "MessageBox".asInstanceOf[ReactClass]
  CopyMoveUi.moveProcessComp = mockUiComponent("MoveProcess")

  private val currTheme = DefaultTheme
  
  //noinspection TypeAnnotation
  class Actions(isLocal: Boolean = true) {
    val getDriveRoot = mockFunction[String, js.Promise[js.UndefOr[String]]]
    val updateDir = mockFunction[Dispatch, String, TaskAction]
    val readDir = mockFunction[String, js.UndefOr[String], js.Promise[FileListDir]]

    val actions = new MockFileListActions(
      new MockFileListApi(
        isLocalMock = isLocal,
        readDirMock = readDir,
        getDriveRootMock = getDriveRoot,
      ),
      updateDirMock = updateDir
    )
  }

  //noinspection TypeAnnotation
  class HistoryMocks {
    val get = mockFunction[HistoryKind, js.Promise[HistoryService]]
    val save = mockFunction[History, js.Promise[Unit]]

    val service = new MockHistoryService(
      saveMock = save
    )
    val provider = new MockHistoryProvider(
      getMock = get
    )
  }
  
  private val driveRootRes = js.Promise.resolve[js.UndefOr[String]](js.undefined: js.UndefOr[String])

  it should "show CopyItemsStats when copy" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir("/to/path", isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), copyItemsStats) {
        case CopyItemsStatsProps(_, resActions, fromPath, items, title, _, _) =>
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe currDir.items.toList
          title shouldBe "Copy"
      }
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponentProps(renderer.root, copyItemsStats).onCancel()
      
      Succeeded
    }
  }

  it should "show CopyItemsStats when move" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir("/to/path", isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    actions.getDriveRoot.expects(currDir.path).returning(driveRootRes)
    actions.getDriveRoot.expects(toDir.path).returning(driveRootRes)
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }
    
    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), copyItemsStats) {
        case CopyItemsStatsProps(_, resActions, fromPath, items, title, _, _) =>
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe currDir.items.toList
          title shouldBe "Move"
      }
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponentProps(renderer.root, copyItemsStats).onCancel()

      Succeeded
    }
  }

  it should "call onClose when onCancel in copyItemsPopup" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)

    //then
    onClose.expects()

    //when
    copyPopup.onCancel()
    
    Succeeded
  }

  it should "render error popup if same path" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(item))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir("/folder", isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertNativeComponent(renderer.root.children.head, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\nonto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }))())
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.head.onAction()

      Succeeded
    }
  }

  it should "render error popup when move into itself" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("folder", isRoot = false, js.Array(item))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir(path.join("folder", "dir 1"), isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    actions.getDriveRoot.expects(currDir.path).returning(driveRootRes)
    actions.getDriveRoot.expects(toDir.path).returning(driveRootRes)
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertNativeComponent(renderer.root.children.head, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot move the item\n${item.name}\ninto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }))())
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.head.onAction()

      Succeeded
    }
  }

  it should "render error popup when copy into itself in sub-folder" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("folder", isRoot = false, js.Array(item))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir(path.join("folder", "dir 1", "dir 2"), isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertNativeComponent(renderer.root.children.head, <(messageBoxComp)(^.assertPlain[MessageBoxProps](inside(_) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\ninto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }))())
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponents(renderer.root, messageBoxComp).head.props.asInstanceOf[MessageBoxProps].actions.head.onAction()

      Succeeded
    }
  }

  it should "render MoveProcess when move within same drive" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir("/to/path", isRoot = false, js.Array())
    val to = "test to path"
    val driveRoot = "same"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    actions.getDriveRoot.expects(currDir.path).returning(js.Promise.resolve[js.UndefOr[String]](driveRoot))
    actions.getDriveRoot.expects(toDir.path).returning(js.Promise.resolve[js.UndefOr[String]](driveRoot))
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), moveProcessComp) {
        case MoveProcessProps(_, resActions, fromPath, items, toPath, _, _) =>
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe List((item, item.name))
          toPath shouldBe toDir.path
      }
    }
  }

  it should "render MoveProcess when move inplace" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveInplace,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val to = "test to path"

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), moveProcessComp) {
        case MoveProcessProps(resDispatch, resActions, fromPath, items, toPath, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe List((item, to))
          toPath shouldBe currDir.path
      }
    }
  }

  it should "render CopyProcess when copy inplace" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyInplace,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val to = "test to path"

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      val total = 123456789
      statsPopup.onDone(total)

      assertTestComponent(renderer.root.children.head, copyProcessComp) {
        case CopyProcessProps(resFrom, resTo, move, fromPath, items, resToPath, resTotal, _, _) =>
          inside(resFrom) { case FileListData(resDispatch, resActions, resState) =>
            resDispatch shouldBe dispatch
            resActions shouldBe actions.actions
            resState shouldBe state
          }
          resTo shouldBe resFrom
          move shouldBe false
          fromPath shouldBe currDir.path
          items shouldBe List((item, to))
          resToPath shouldBe currDir.path
          resTotal shouldBe total
      }
    }
  }

  it should "render CopyProcess when move" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val toDir = FileListDir("/folder/dir to", isRoot = false, js.Array())
    val to = "test to path"

    //then
    actions.readDir.expects(currDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    actions.getDriveRoot.expects(currDir.path).returning(driveRootRes)
    actions.getDriveRoot.expects(toDir.path).returning(driveRootRes)
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      val total = 123456789
      statsPopup.onDone(total)

      assertTestComponent(renderer.root.children.head, copyProcessComp) {
        case CopyProcessProps(resFrom, resTo, move, fromPath, items, resToPath, resTotal, _, _) =>
          inside(resFrom) { case FileListData(_, resActions, resState) =>
            resActions shouldBe actions.actions
            resState shouldBe state
          }
          inside(resTo) { case FileListData(resDispatch, resActions, resState) =>
            resDispatch shouldBe toDispatch
            resActions shouldBe toActions
            resState shouldBe toState
          }
          move shouldBe true
          fromPath shouldBe currDir.path
          items shouldBe List((item, item.name))
          resToPath shouldBe toDir.path
          resTotal shouldBe total
      }
    }
  }

  it should "render CopyProcess when move from virtual FS" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new Actions(isLocal = false)
    val item = FileListItem("dir", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir)
    val toDispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val to = "test/to/path"

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      val total = 123456789
      statsPopup.onDone(total)

      assertTestComponent(renderer.root.children.head, copyProcessComp) {
        case CopyProcessProps(resFrom, resTo, move, fromPath, items, resToPath, resTotal, _, _) =>
          inside(resFrom) { case FileListData(resDispatch, resActions, resState) =>
            resDispatch shouldBe dispatch
            resActions shouldBe actions.actions
            resState shouldBe state
          }
          inside(resTo) { case FileListData(resDispatch, resActions, resState) =>
            resDispatch shouldBe toDispatch
            resActions shouldBe toActions
            resState shouldBe toState
          }
          move shouldBe true
          fromPath shouldBe currDir.path
          items shouldBe List((item, item.name))
          resToPath shouldBe to
          resTotal shouldBe total
      }
    }
  }

  it should "dispatch FileListParamsChangedAction if selected when onDone" in {
    //given
    val fromDispatch = mockFunction[js.Any, Unit]
    val fromActions = new Actions
    val dir = FileListItem("dir 1", isDir = true)
    val leftDir = FileListDir("/left/dir", isRoot = false, js.Array(
      FileListItem.up,
      dir,
      FileListItem("file 1")
    ))
    val rightDir = FileListDir("/right/dir", isRoot = false, js.Array(FileListItem("dir 2", isDir = true)))
    val fromState = FileListState(index = 1, currDir = leftDir, selectedNames = js.Set(dir.name, "file 1"))
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(fromDispatch, fromActions.actions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions.actions, toState))
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)(), historyMocks.provider))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)

    val toDir = FileListDir("/to/path/dir 1", isRoot = false, js.Array())
    val to = "test to path"
    fromActions.readDir.expects(leftDir.path, to: js.UndefOr[String]).returning(js.Promise.resolve[FileListDir](toDir))
    fromDispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        action.task.message shouldBe "Resolving target dir"
      }
      ()
    }
    copyPopup.onAction(to)
    
    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      statsPopup.onDone(123)

      findProps(renderer.root, copyProcessComp) should not be empty
    }.flatMap { _ =>
      val progressPopup = findComponentProps(renderer.root, copyProcessComp)
      progressPopup.onTopItem(dir)

      val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
        FileListItem("file 1")
      ))
      val leftAction = TaskAction(Task("Updating", Future.successful(updatedDir)))
      val rightAction = TaskAction(Task("Updating", Future.successful(updatedDir)))

      //then
      fromDispatch.expects(*).onCall { action: Any =>
        assertFileListParamsChangedAction(action,
          FileListParamsChangedAction(
            offset = 0,
            index = 1,
            selectedNames = js.Set("file 1")
          )
        )
        ()
      }
      onClose.expects()
      var saveHistory: History = null
      historyMocks.get.expects(copyItemsHistoryKind)
        .returning(js.Promise.resolve[HistoryService](historyMocks.service))
      historyMocks.save.expects(*).onCall { h: History =>
        saveHistory = h
        js.Promise.resolve[Unit](())
      }
      fromActions.updateDir.expects(*, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(*, rightDir.path).returning(rightAction)
      fromDispatch.expects(leftAction)
      toDispatch.expects(rightAction)

      //when
      progressPopup.onDone()

      //then
      for {
        _ <- leftAction.task.result.toFuture
        _ <- rightAction.task.result.toFuture
        _ <- eventually(saveHistory should not be null)
      } yield {
        inside(saveHistory) {
          case History(item, params) =>
            item shouldBe to
            params shouldBe js.undefined
        }
      }
    }
  }

  it should "dispatch FileListItemCreatedAction if inplace when onDone" in {
    //given
    val fromDispatch = mockFunction[js.Any, Unit]
    val fromActions = new Actions
    val dir = FileListItem("dir 1", isDir = true)
    val leftDir = FileListDir("/left/dir", isRoot = false, js.Array(
      FileListItem.up,
      dir,
      FileListItem("file 1")
    ))
    val rightDir = FileListDir("/right/dir", isRoot = false, js.Array(FileListItem("dir 2", isDir = true)))
    val fromState = FileListState(index = 1, currDir = leftDir, selectedNames = js.Set("file 1"))
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyInplace,
      from = FileListData(fromDispatch, fromActions.actions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions.actions, toState))
    )
    val historyMocks = new HistoryMocks
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)(), historyMocks.provider))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup, plain = true)
    val to = "test to path"
    copyPopup.onAction(to)

    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      statsPopup.onDone(123)

      findProps(renderer.root, copyProcessComp) should not be empty
    }.flatMap { _ =>
      val progressPopup = findComponentProps(renderer.root, copyProcessComp)
      progressPopup.onTopItem(dir)

      val updatedDir = FileListDir("/updated/dir", isRoot = false, js.Array(
        FileListItem("file 1")
      ))
      val leftAction = TaskAction(Task("Updating", Future.successful(updatedDir)))

      //then
      onClose.expects()
      var saveHistory: History = null
      historyMocks.get.expects(copyItemsHistoryKind)
        .returning(js.Promise.resolve[HistoryService](historyMocks.service))
      historyMocks.save.expects(*).onCall { h: History =>
        saveHistory = h
        js.Promise.resolve[Unit](())
      }
      fromActions.updateDir.expects(*, leftDir.path).returning(leftAction)
      fromDispatch.expects(leftAction)
      fromDispatch.expects(*).onCall { action: Any =>
        assertFileListItemCreatedAction(action, FileListItemCreatedAction(to, updatedDir))
        ()
      }

      //when
      progressPopup.onDone()

      //then
      for {
        _ <- leftAction.task.result.toFuture
        _ <- eventually(saveHistory should not be null)
      } yield {
        inside(saveHistory) {
          case History(item, params) =>
            item shouldBe to
            params shouldBe js.undefined
        }
      }
    }
  }

  it should "render CopyItemsPopup(items=single) when copy to different dir" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array(item)))
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/test-path", isRoot = false, js.Array()))
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )

    //when
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))

    //then
    inside(renderer.root.children.toList) { case List(result) =>
      assertTestComponent(result, copyItemsPopup, plain = true) {
        case CopyItemsPopupProps(move, path, items, _, _) =>
          move shouldBe false
          path shouldBe "/test-path"
          items.toList shouldBe Seq(item)
      }
    }
  }

  it should "render CopyItemsPopup(items=single) when copy to the same dir" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array(item)))
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array()))
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(dispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(dispatch, actions.actions, state),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )

    //when
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))

    //then
    inside(renderer.root.children.toList) { case List(result) =>
      assertTestComponent(result, copyItemsPopup, plain = true) {
        case CopyItemsPopupProps(move, path, items, _, _) =>
          move shouldBe false
          path shouldBe item.name
          items.toList shouldBe Seq(item)
      }
    }
  }

  it should "render CopyItemsPopup(items=multi) when move to different dir" in {
    //given
    val fromDispatch = mockFunction[js.Any, Unit]
    val fromActions = new MockFileListActions
    val items = js.Array(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/test-path", isRoot = false, items),
      selectedNames = js.Set("file 1", "dir 1")
    )
    val toDispatch = mockFunction[js.Any, Unit]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array()))
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(fromDispatch, fromActions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )

    //when
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))

    //then
    inside(renderer.root.children.toList) { case List(result) =>
      assertTestComponent(result, copyItemsPopup, plain = true) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe "/folder"
          resItems.toList shouldBe items.toList
      }
    }
  }

  it should "render CopyItemsPopup(items=multi) when move to the same dir" in {
    //given
    val fromDispatch = mockFunction[js.Any, Unit]
    val fromActions = new MockFileListActions
    val items = js.Array(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/folder", isRoot = false, items),
      selectedNames = js.Set("file 1", "dir 1")
    )
    val toDispatch = mockFunction[js.Any, Unit]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array()))
    val toActions = new MockFileListActions
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveToTarget,
      from = FileListData(fromDispatch, fromActions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )

    //when
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))

    //then
    inside(renderer.root.children.toList) { case List(result) =>
      assertTestComponent(result, copyItemsPopup, plain = true) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe "/folder"
          resItems.toList shouldBe items.toList
      }
    }
  }

  it should "render CopyItemsPopup when move inplace" in {
    //given
    val fromDispatch = mockFunction[js.Any, Unit]
    val fromActions = new MockFileListActions
    val item = FileListItem("file 1")
    val items = js.Array(
      item,
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/test-path", isRoot = false, items),
      selectedNames = js.Set("file 1", "dir 1")
    )
    val toActions = new MockFileListActions
    val toDispatch = mockFunction[js.Any, Unit]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, js.Array()))
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowMoveInplace,
      from = FileListData(fromDispatch, fromActions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions, toState))
    )

    //when
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)()))

    //then
    inside(renderer.root.children.toList) { case List(result) =>
      assertTestComponent(result, copyItemsPopup, plain = true) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe item.name
          resItems.toList shouldBe List(item)
      }
    }
  }
}

object CopyMoveUiSpec {

  def withContext(element: ReactElement,
                  historyProvider: HistoryProvider = new MockHistoryProvider
                 ): ReactElement = {

    HistoryProviderSpec.withHistoryProvider(withThemeContext(element), historyProvider)
  }
}
