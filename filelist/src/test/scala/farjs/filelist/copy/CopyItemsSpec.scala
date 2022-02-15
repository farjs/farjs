package farjs.filelist.copy

import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.copy.CopyItems._
import farjs.filelist.fs.{FSDisk, MockFSService}
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.{FileListPopupsProps, FileListPopupsState}
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.ui.popup.MessageBoxProps
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask
import scommons.react.test._

import scala.concurrent.Future

class CopyItemsSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  CopyItems.copyItemsStats = mockUiComponent("CopyItemsStats")
  CopyItems.copyItemsPopup = mockUiComponent("CopyItemsPopup")
  CopyItems.copyProcessComp = mockUiComponent("CopyProcess")
  CopyItems.messageBoxComp = mockUiComponent("MessageBox")
  CopyItems.moveProcessComp = mockUiComponent("MoveProcess")
  
  //noinspection TypeAnnotation
  class Actions {
    val updateDir = mockFunction[Dispatch, Boolean, String, FileListDirUpdateAction]
    val readDir = mockFunction[Option[String], String, Future[FileListDir]]

    val actions = new MockFileListActions(
      updateDirMock = updateDir,
      readDirMock = readDir
    )
  }

  //noinspection TypeAnnotation
  class FsService {
    val readDisk = mockFunction[String, Future[Option[FSDisk]]]

    val fsService = new MockFSService(
      readDiskMock = readDisk
    )
  }

  it should "show CopyItemsStats when copy" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

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
      //when & then
      findComponentProps(renderer.root, copyItemsStats).onCancel()
      renderer.root.children.toList should be (empty)
    }
  }

  it should "show CopyItemsStats when move" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val fsService = new FsService
    CopyItems.fsService = fsService.fsService
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    fsService.readDisk.expects(currDir.path).returning(Future.successful(None))
    fsService.readDisk.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    
    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

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
      //when & then
      findComponentProps(renderer.root, copyItemsStats).onCancel()
      renderer.root.children.toList should be (empty)
    }
  }

  it should "hide CopyItemsPopup when onCancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    //then
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))

    //when
    copyPopup.onCancel()
    
    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))(), leftStack, rightStack))
    }

    renderer.root.children.toList should be (empty)
  }

  it should "render error popup if same path" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(item))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\nonto itself"
          inside(resActions) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe Theme.current.popup.error
      }
    }.map { _ =>
      //when & then
      findComponentProps(renderer.root, messageBoxComp).actions.head.onAction()
      renderer.root.children.toList should be (empty)
    }
  }

  it should "render error popup when move into itself" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val fsService = new FsService
    CopyItems.fsService = fsService.fsService
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(item))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder/dir 1", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    fsService.readDisk.expects(currDir.path).returning(Future.successful(None))
    fsService.readDisk.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot move the item\n${item.name}\ninto itself"
          inside(resActions) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe Theme.current.popup.error
      }
    }.map { _ =>
      //when & then
      findComponentProps(renderer.root, messageBoxComp).actions.head.onAction()
      renderer.root.children.toList should be (empty)
    }
  }

  it should "render error popup when copy into itself in sub-folder" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(item))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder/dir 1/dir 2", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      assertTestComponent(renderer.root.children.head, messageBoxComp) {
        case MessageBoxProps(title, message, resActions, style) =>
          title shouldBe "Error"
          message shouldBe s"Cannot copy the item\n${item.name}\ninto itself"
          inside(resActions) { case List(ok) =>
            ok.label shouldBe "OK"
          }
          style shouldBe Theme.current.popup.error
      }
    }.map { _ =>
      //when & then
      findComponentProps(renderer.root, messageBoxComp).actions.head.onAction()
      renderer.root.children.toList should be (empty)
    }
  }

  it should "render MoveProcess when move within same drive" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val fsService = new FsService
    CopyItems.fsService = fsService.fsService
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"
    val sameDrive = FSDisk("same", 0, 0, "SameDrive")

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    fsService.readDisk.expects(currDir.path).returning(Future.successful(Some(sameDrive)))
    fsService.readDisk.expects(toDir.path).returning(Future.successful(Some(sameDrive)))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

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
    val actions = mock[FileListActions]
    val fsService = new FsService
    CopyItems.fsService = fsService.fsService
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveInplace)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val to = "test to path"

    //then
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      assertTestComponent(renderer.root.children(0), moveProcessComp) {
        case MoveProcessProps(resDispatch, resActions, fromPath, items, toPath, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
          fromPath shouldBe currDir.path
          items shouldBe List((item, to))
          toPath shouldBe currDir.path
      }
    }
  }

  it should "render CopyProcess when copy inplace" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyInplace)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val to = "test to path"

    //then
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      val total = 123456789
      statsPopup.onDone(total)

      assertTestComponent(renderer.root.children.head, copyProcessComp) {
        case CopyProcessProps(resDispatch, resActions, move, fromPath, items, resToPath, resTotal, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions
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
    val fsService = new FsService
    CopyItems.fsService = fsService.fsService
    val item = FileListItem("dir", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder/dir to", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    fsService.readDisk.expects(currDir.path).returning(Future.successful(None))
    fsService.readDisk.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

    eventually {
      val statsPopup = findComponentProps(renderer.root, copyItemsStats)
      val total = 123456789
      statsPopup.onDone(total)

      assertTestComponent(renderer.root.children.head, copyProcessComp) {
        case CopyProcessProps(resDispatch, resActions, move, fromPath, items, resToPath, resTotal, _, _) =>
          resDispatch shouldBe dispatch
          resActions shouldBe actions.actions
          move shouldBe true
          fromPath shouldBe currDir.path
          items shouldBe List((item, item.name))
          resToPath shouldBe toDir.path
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
    val props = FileListPopupsProps(fromDispatch, fromActions.actions, FileListsState(
      left = FileListState(index = 1, currDir = leftDir, isActive = true, selectedNames = Set(dir.name, "file 1")),
      right = FileListState(currDir = rightDir, isRight = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions.actions), Some(props.data.left))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions.actions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    val toDir = FileListDir("/to/path/dir 1", isRoot = false, Nil)
    val to = "test to path"
    fromActions.readDir.expects(Some(leftDir.path), to).returning(Future.successful(toDir))
    fromDispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    fromDispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    copyPopup.onAction(to)
    
    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          right = props.data.right,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

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
        isRight = false,
        offset = 0,
        index = 1,
        selectedNames = Set("file 1")
      ))
      fromActions.updateDir.expects(fromDispatch, false, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(toDispatch, true, rightDir.path).returning(rightAction)
      fromDispatch.expects(leftAction)
      toDispatch.expects(rightAction)

      //when
      progressPopup.onDone()

      //then
      findProps(renderer.root, copyProcessComp) should be(empty)

      for {
        _ <- leftAction.task.future
        _ <- rightAction.task.future
      } yield Succeeded
    }
  }

  it should "not dispatch FileListParamsChangedAction if not selected when onDone" in {
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
    val props = FileListPopupsProps(fromDispatch, fromActions.actions, FileListsState(
      left = FileListState(index = 1, currDir = leftDir, isActive = true, selectedNames = Set("file 1")),
      right = FileListState(currDir = rightDir, isRight = true),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions.actions), Some(props.data.left))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions.actions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    val toDir = FileListDir("/to/path/dir 1", isRoot = false, Nil)
    val to = "test to path"
    fromActions.readDir.expects(Some(leftDir.path), to).returning(Future.successful(toDir))
    fromDispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    fromDispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    copyPopup.onAction("test to path")

    TestRenderer.act { () =>
      renderer.update(withContext(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          right = props.data.right,
          popups = FileListPopupsState()
        )
      ))(), leftStack, rightStack))
    }

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
        isRight = false,
        offset = 0,
        index = 1,
        selectedNames = Set("file 1")
      )).never()
      fromActions.updateDir.expects(fromDispatch, false, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(toDispatch, true, rightDir.path).returning(rightAction)
      fromDispatch.expects(leftAction)
      toDispatch.expects(rightAction)

      //when
      progressPopup.onDone()

      //then
      findProps(renderer.root, copyProcessComp) should be(empty)

      for {
        _ <- leftAction.task.future
        _ <- rightAction.task.future
      } yield Succeeded
    }
  }

  it should "render empty component when showCopyMovePopup=CopyMoveHidden" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState())
    props.data.popups.showCopyMovePopup shouldBe CopyMoveHidden
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    result.children.toList should be (empty)
  }
  
  it should "render CopyItemsPopup(items=single) when copy to different dir" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val item = FileListItem("dir 1", isDir = true)
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true),
      right = FileListState(currDir = FileListDir("/test-path", isRoot = false, Nil)),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)

    //when
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )

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
    val actions = mock[FileListActions]
    val item = FileListItem("dir 1", isDir = true)
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true),
      right = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil)),
      popups = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    ))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.right))
    ), null)

    //when
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )

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
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil)),
      right = FileListState(
        currDir = FileListDir("/test-path", isRoot = false, items),
        isActive = true,
        selectedNames = Set("file 1", "dir 1")
      ),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.right))
    ), null)

    //when
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )

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
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val items = List(
      FileListItem("file 1"),
      FileListItem("dir 1", isDir = true)
    )
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil)),
      right = FileListState(
        currDir = FileListDir("/folder", isRoot = false, items),
        isActive = true,
        selectedNames = Set("file 1", "dir 1")
      ),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    ))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.right))
    ), null)

    //when
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )

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
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val item = FileListItem("file 1")
    val items = List(
      item,
      FileListItem("dir 1", isDir = true)
    )
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil)),
      right = FileListState(
        currDir = FileListDir("/test-path", isRoot = false, items),
        isActive = true,
        selectedNames = Set("file 1", "dir 1")
      ),
      popups = FileListPopupsState(showCopyMovePopup = ShowMoveInplace)
    ))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(mockFunction[Any, Any]), Some(new MockFileListActions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(props.data.right))
    ), null)

    //when
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )

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
