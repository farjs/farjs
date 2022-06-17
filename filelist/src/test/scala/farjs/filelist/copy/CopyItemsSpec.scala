package farjs.filelist.copy

import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.copy.CopyItems._
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.popups.FileListPopupsState
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.ui.popup.MessageBoxProps
import farjs.ui.theme.Theme
import org.scalatest.Succeeded
import scommons.nodejs.path
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

  it should "show CopyItemsStats when copy" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })
    
    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
    }

    renderer.root.children.toList should be (empty)
  }

  it should "render error popup if same path" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir(path.join("folder", "dir 1"), isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val currDir = FileListDir("folder", isRoot = false, List(item))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir(path.join("folder", "dir 1", "dir 2"), isRoot = false, Nil)
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/to/path", isRoot = false, Nil)
    val to = "test to path"
    val driveRoot = "same"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(Some(driveRoot)))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(Some(driveRoot)))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      item,
      FileListItem("file 1")
    ))
    val state = FileListState(currDir = currDir, isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveInplace)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
    }

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
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyInplace)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
    }

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
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toDir = FileListDir("/folder/dir to", isRoot = false, Nil)
    val to = "test to path"

    //then
    actions.readDir.expects(Some(currDir.path), to).returning(Future.successful(toDir))
    actions.getDriveRoot.expects(currDir.path).returning(Future.successful(None))
    actions.getDriveRoot.expects(toDir.path).returning(Future.successful(None))
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))
    dispatch.expects(*).onCall(inside(_: Any) { case action: FileListTaskAction =>
      action.task.message shouldBe "Resolving target dir"
    })

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
    }

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
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(CopyItems())(^.wrapped := props)(), leftStack, rightStack)
    )
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val to = "test/to/path"

    //then
    dispatch.expects(FileListPopupCopyMoveAction(CopyMoveHidden))

    //when
    copyPopup.onAction(to)

    //then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
    }

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
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions.actions), Some(fromState))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions.actions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
        offset = 0,
        index = 1,
        selectedNames = Set("file 1")
      ))
      fromActions.updateDir.expects(fromDispatch, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(toDispatch, rightDir.path).returning(rightAction)
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
    val fromState = FileListState(index = 1, currDir = leftDir, isActive = true, selectedNames = Set("file 1"))
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions.actions), Some(fromState))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new Actions
    val toState = FileListState(currDir = rightDir)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions.actions), Some(toState))
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
      renderer.update(
        withContext(<(CopyItems())(^.wrapped := FileListPopupsState())(), leftStack, rightStack)
      )
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
        offset = 0,
        index = 1,
        selectedNames = Set("file 1")
      )).never()
      fromActions.updateDir.expects(fromDispatch, leftDir.path).returning(leftAction)
      toActions.updateDir.expects(toDispatch, rightDir.path).returning(rightAction)
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
    val actions = new Actions
    val state = FileListState()
    val props = FileListPopupsState()
    props.showCopyMovePopup shouldBe CopyMoveHidden
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState()
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/test-path", isRoot = false, Nil))
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
    val actions = new Actions
    val item = FileListItem("dir 1", isDir = true)
    val state = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true)
    val props = FileListPopupsState(showCopyMovePopup = ShowCopyToTarget)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(state))
    ), null)
    val toDispatch = mockFunction[Any, Any]
    val toActions = new MockFileListActions
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
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
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions), Some(fromState))
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
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveToTarget)
    val toDispatch = mockFunction[Any, Any]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
    val toActions = new MockFileListActions
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions), Some(fromState))
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
    val props = FileListPopupsState(showCopyMovePopup = ShowMoveInplace)
    val toActions = new MockFileListActions
    val toDispatch = mockFunction[Any, Any]
    val toState = FileListState(currDir = FileListDir("/folder", isRoot = false, Nil))
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], Some(toDispatch), Some(toActions), Some(toState))
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(fromDispatch), Some(fromActions), Some(fromState))
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
