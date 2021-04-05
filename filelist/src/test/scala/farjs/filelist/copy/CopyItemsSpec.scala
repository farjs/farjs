package farjs.filelist.copy

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.copy.CopyItems._
import farjs.filelist.popups.FileListPopupsActions.FileListPopupCopyItemsAction
import farjs.filelist.popups.{FileListPopupsProps, FileListPopupsState}
import scommons.react._
import scommons.react.test._

class CopyItemsSpec extends TestSpec with TestRendererUtils {

  CopyItems.copyItemsStats = () => "CopyItemsStats".asInstanceOf[ReactClass]
  CopyItems.copyItemsPopup = () => "CopyItemsPopup".asInstanceOf[ReactClass]
  CopyItems.copyProcessComp = () => "CopyProcess".asInstanceOf[ReactClass]
  
  it should "show CopyItemsStats when showCopyItemsPopup=true" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))

    //when
    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())

    //then
    assertTestComponent(renderer.root.children(0), copyItemsStats) {
      case CopyItemsStatsProps(resDispatch, resActions, state, _, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        state shouldBe props.data.activeList
    }
  }

  it should "hide CopyItemsStats when onCancel" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))
    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)

    //then
    dispatch.expects(FileListPopupCopyItemsAction(show = false))
    
    //when
    statsPopup.onCancel()

    //then
    TestRenderer.act { () =>
      renderer.update(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))())
    }

    renderer.root.children.toList should be (empty)
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
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))
    
    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)
    statsPopup.onDone(123)
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    //then
    dispatch.expects(FileListPopupCopyItemsAction(show = false))

    //when
    copyPopup.onCancel()
    
    //then
    TestRenderer.act { () =>
      renderer.update(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))())
    }

    renderer.root.children.toList should be (empty)
  }

  it should "render CopyProcess when onCopy" in {
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
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))

    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)
    val total = 123456789
    statsPopup.onDone(total)
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)
    val toPath = "test to path"

    //then
    dispatch.expects(FileListPopupCopyItemsAction(show = false))

    //when
    copyPopup.onCopy(toPath)

    //then
    TestRenderer.act { () =>
      renderer.update(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(
          left = props.data.left,
          popups = FileListPopupsState()
        )
      ))())
    }

    assertTestComponent(renderer.root.children.head, copyProcessComp) {
      case CopyProcessProps(resDispatch, resActions, fromPath, items, resToPath, resTotal, _) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        fromPath shouldBe currDir.path
        items shouldBe List(item)
        resToPath shouldBe toPath
        resTotal shouldBe total
    }
  }

  it should "hide CopyProcess when onDone" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))

    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)
    statsPopup.onDone(123)
    val copyPopup = findComponentProps(renderer.root, copyItemsPopup)

    dispatch.expects(FileListPopupCopyItemsAction(show = false))

    copyPopup.onCopy("test to path")
    val progressPopup = findComponentProps(renderer.root, copyProcessComp)

    //when
    progressPopup.onDone()
    
    //then
    TestRenderer.act { () =>
      renderer.update(<(CopyItems())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))())
    }

    findProps(renderer.root, copyProcessComp) should be (empty)
  }

  it should "render empty component when showCopyItemsPopup=false" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState())

    //when
    val result = createTestRenderer(<(CopyItems())(^.wrapped := props)()).root

    //then
    result.children.toList should be (empty)
  }
  
  it should "render CopyItemsPopup(items=single)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val item = FileListItem("dir 1", isDir = true)
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = FileListDir("/folder", isRoot = false, List(item)), isActive = true),
      right = FileListState(currDir = FileListDir("/test-path", isRoot = false, Nil)),
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))
    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)

    //when
    statsPopup.onDone(123)

    //then
    assertTestComponent(renderer.root.children(0), copyItemsPopup) {
      case CopyItemsPopupProps(path, items, _, _) =>
        path shouldBe "/test-path"
        items shouldBe Seq(item)
    }
  }

  it should "render CopyItemsPopup(items=multiple)" in {
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
      popups = FileListPopupsState(showCopyItemsPopup = true)
    ))

    val renderer = createTestRenderer(<(CopyItems())(^.wrapped := props)())
    val statsPopup = findComponentProps(renderer.root, copyItemsStats)

    //when
    statsPopup.onDone(123)

    //then
    assertTestComponent(renderer.root.children(0), copyItemsPopup) {
      case CopyItemsPopupProps(path, resItems, _, _) =>
        path shouldBe "/folder"
        resItems shouldBe items
    }
  }
}
