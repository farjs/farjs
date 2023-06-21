package farjs.copymove

import farjs.copymove.CopyMoveUi._
import farjs.copymove.CopyMoveUiAction._
import farjs.copymove.CopyMoveUiSpec._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.history.{FileListHistoryService, MockFileListHistoryService}
import farjs.ui.Dispatch
import farjs.ui.popup.MessageBoxProps
import farjs.ui.task.FutureTask
import farjs.ui.theme.DefaultTheme
import farjs.ui.theme.ThemeSpec.withThemeContext
import org.scalatest.Succeeded
import scommons.nodejs.path
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.Future

class CopyMoveUiSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyMoveUi.copyItemsStats = mockUiComponent("CopyItemsStats")
  CopyMoveUi.copyItemsPopup = mockUiComponent("CopyItemsPopup")
  CopyMoveUi.copyProcessComp = mockUiComponent("CopyProcess")
  CopyMoveUi.messageBoxComp = mockUiComponent("MessageBox")
  CopyMoveUi.moveProcessComp = mockUiComponent("MoveProcess")

  private val currTheme = DefaultTheme
  
  //noinspection TypeAnnotation
  class Actions(isLocalFS: Boolean = true) {
    val getDriveRoot = mockFunction[String, Future[Option[String]]]
    val updateDir = mockFunction[Dispatch, String, FileListDirUpdateAction]
    val readDir = mockFunction[Option[String], String, Future[FileListDir]]

    val actions = new MockFileListActions(
      isLocalFSMock = isLocalFS,
      getDriveRootMock = getDriveRoot,
      updateDirMock = updateDir,
      readDirMock = readDir
    )
  }

  //noinspection TypeAnnotation
  class HistoryService {
    val save = mockFunction[String, Future[Unit]]

    val service = new MockFileListHistoryService(
      saveMock = save
    )
  }

  it should "show CopyItemsStats when copy" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), copyItemsStats) {
        case CopyItemsStatsProps(resDispatch, resActions, fromPath, items, title, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe currDir.items
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
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    
    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), copyItemsStats) {
        case CopyItemsStatsProps(resDispatch, resActions, fromPath, items, title, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe currDir.items
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
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    //then
    onClose.expects()

    //when
    copyPopup.onCancel()
    
    Succeeded
  }

  it should "render error popup if same path" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp, plain = true) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\nonto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponentProps(renderer.root, messageBoxComp, plain = true).actions.head.onAction()

      Succeeded
    }
  }

  it should "render error popup when move into itself" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir(path.join("folder", "dir 1"), isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp, plain = true) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot move the item\n${item.name}\ninto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponentProps(renderer.root, messageBoxComp, plain = true).actions.head.onAction()

      Succeeded
    }
  }

  it should "render error popup when copy into itself in sub-folder" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir(path.join("folder", "dir 1", "dir 2"), isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp, plain = true) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\ninto itself"
          inside(resActions.toList) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe currTheme.popup.error
      }
    }.map { _ =>
      //then
      onClose.expects()

      //when
      findComponentProps(renderer.root, messageBoxComp, plain = true).actions.head.onAction()

      Succeeded
    }
  }

  it should "render MoveProcess when move within same drive" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"
    val driveRoot = "same"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(Some(driveRoot)))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(Some(driveRoot)))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    eventually {
      assertTestComponent(renderer.root.children(0), moveProcessComp) {
        case MoveProcessProps(resDispatch, resActions, fromPath, items, toPath, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions.actions
          fromPath shouldBe currDir.path
          items shouldBe List((item, item.name))
          toPath shouldBe toDir.path
      }
    }
  }

  it should "render MoveProcess when move inplace" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
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
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
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
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder/dir to", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

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
          resToPath shouldBe toDir.path
          resTotal shouldBe total
      }
    }
  }

  it should "render CopyProcess when move from virtual FS" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions(isLocalFS = false)
    val item = FileListItem("dir", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val toDispatch = mockFunction[Any, Any]
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
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
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
    val fromDispatch = mockFunction[Any, Any]
    val fromActions = new Actions
    val dir = FileListItem("dir 1", isDir = true)
    val leftDir = FileListDir("/left/dir", isRoot = false, List(
      FileListItem.up,
      dir,
      FileListItem("file 1")
    ))
    val rightDir = FileListDir("/right/dir", isRoot = false, List(FileListItem("dir 2", isDir = true)))
    val fromState = FileListState(index = 1, currDir = leftDir, isActive = true, selectedNames = Set(dir.name, "file 1"))
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyToTarget,
      from = FileListData(fromDispatch, fromActions.actions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions.actions, toState))
    )
    val historyService = new HistoryService
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)(), historyService.service))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    val toDir = FileListDir("/to/path/dir 1", isRoot = false, Nil)
    val to = "test to path"
    fromActions.readDir.expects(Some(leftDir.path), to).returning(Future.successful(toDir))
    fromDispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    copyPopup.onAction(to)
    
    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      statsPopup.onDone(123)

      findProps(renderer.root, copyProcessComp) should not be empty
    }.flatMap { _ =>
      val progressPopup = findComponentProps(renderer.root, copyProcessComp)
      progressPopup.onTopItem(dir)

      val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
        FileListItem("file 1")
      ))
      val leftAction = FileListDirUpdateAction(FutureTask("Updating", Future.successful(updatedDir)))
      val rightAction = FileListDirUpdateAction(FutureTask("Updating", Future.successful(updatedDir)))

      //then
      fromDispatch.expects(FileListParamsChangedAction(
        offset = 0,
        index = 1,
        selectedNames = Set("file 1")
      ))
      onClose.expects()
      historyService.save.expects(to).returning(Future.unit)
      fromActions.updateDir.expects(fromDispatch, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(toDispatch, rightDir.path).returning(rightAction)
      fromDispatch.expects(leftAction)
      toDispatch.expects(rightAction)

      //when
      progressPopup.onDone()

      //then
      for {
        _ <- leftAction.task.future
        _ <- rightAction.task.future
      } yield Succeeded
    }
  }

  it should "dispatch FileListItemCreatedAction if inplace when onDone" in {
    //given
    val fromDispatch = mockFunction[Any, Any]
    val fromActions = new Actions
    val dir = FileListItem("dir 1", isDir = true)
    val leftDir = FileListDir("/left/dir", isRoot = false, List(
      FileListItem.up,
      dir,
      FileListItem("file 1")
    ))
    val rightDir = FileListDir("/right/dir", isRoot = false, List(FileListItem("dir 2", isDir = true)))
    val fromState = FileListState(index = 1, currDir = leftDir, isActive = true, selectedNames = Set("file 1"))
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val onClose = mockFunction[Unit]
    val props = FileListPluginUiProps(fromDispatch, onClose)
    val copyMoveUi = new CopyMoveUi(
      show = ShowCopyInplace,
      from = FileListData(fromDispatch, fromActions.actions, fromState),
      maybeTo = Some(FileListData(toDispatch, toActions.actions, toState))
    )
    val historyService = new HistoryService
    val renderer = createTestRenderer(withContext(<(copyMoveUi())(^.plain := props)(), historyService.service))
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val to = "test to path"
    copyPopup.onAction(to)

    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      statsPopup.onDone(123)

      findProps(renderer.root, copyProcessComp) should not be empty
    }.flatMap { _ =>
      val progressPopup = findComponentProps(renderer.root, copyProcessComp)
      progressPopup.onTopItem(dir)

      val updatedDir = FileListDir("/updated/dir", isRoot = false, List(
        FileListItem("file 1")
      ))
      val leftAction = FileListDirUpdateAction(FutureTask("Updating", Future.successful(updatedDir)))

      //then
      onClose.expects()
      historyService.save.expects(to).returning(Future.unit)
      fromActions.updateDir.expects(fromDispatch, leftDir.path).returning(leftAction)
      fromDispatch.expects(leftAction)
      fromDispatch.expects(FileListItemCreatedAction(to, updatedDir))

      //when
      progressPopup.onDone()

      //then
      leftAction.task.future.map(_ => Succeeded)
    }
  }

  it should "render CopyItemsPopup(items=single) when copy to different dir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/test-path", isRoot = false, Nil))
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
      assertTestComponent(result, copyItemsPopup) {
        case CopyItemsPopupProps(move, path, items, _, _) =>
          move shouldBe false
          path shouldBe "/test-path"
          items shouldBe Seq(item)
      }
    }
  }

  it should "render CopyItemsPopup(items=single) when copy to the same dir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
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
      assertTestComponent(result, copyItemsPopup) {
        case CopyItemsPopupProps(move, path, items, _, _) =>
          move shouldBe false
          path shouldBe item.name
          items shouldBe Seq(item)
      }
    }
  }

  it should "render CopyItemsPopup(items=multi) when move to different dir" in {
    //given
    val fromDispatch = mockFunction[Any, Any]
    val fromActions = mock[FileListActions]
    val items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/test-path", isRoot = false, items),
      isActive = true,
      selectedNames = Set("file 1", "dir 1")
    )
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
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
      assertTestComponent(result, copyItemsPopup) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe "/folder"
          resItems shouldBe items
      }
    }
  }

  it should "render CopyItemsPopup(items=multi) when move to the same dir" in {
    //given
    val fromDispatch = mockFunction[Any, Any]
    val fromActions = mock[FileListActions]
    val items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/folder", isRoot = false, items),
      isActive = true,
      selectedNames = Set("file 1", "dir 1")
    )
    val toDispatch = mockFunction[Any, Any]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
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
      assertTestComponent(result, copyItemsPopup) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe "/folder"
          resItems shouldBe items
      }
    }
  }

  it should "render CopyItemsPopup when move inplace" in {
    //given
    val fromDispatch = mockFunction[Any, Any]
    val fromActions = mock[FileListActions]
    val item = FileListItem("file 1")
    val items = List(
      item,
      FileListItem("dir 1", isDir = true)
    )
    val fromState = FileListState(
      currDir = FileListDir("/test-path", isRoot = false, items),
      isActive = true,
      selectedNames = Set("file 1", "dir 1")
    )
    val toActions = new MockFileListActions
    val toDispatch = mockFunction[Any, Any]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
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
      assertTestComponent(result, copyItemsPopup) {
        case CopyItemsPopupProps(move, path, resItems, _, _) =>
          move shouldBe true
          path shouldBe item.name
          resItems shouldBe List(item)
      }
    }
  }
}

object CopyMoveUiSpec {

  def withContext(element: ReactElement,
                  copyItemsHistory: FileListHistoryService = new MockFileListHistoryService
                 ): ReactElement = {

    FileListServicesSpec.withServicesContext(withThemeContext(element), copyItemsHistory = copyItemsHistory)
  }
}
