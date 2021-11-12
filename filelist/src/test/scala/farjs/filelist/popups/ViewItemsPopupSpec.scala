package farjs.filelist.popups

import farjs.filelist.FileListActions.{FileListTaskAction, FileListItemsViewedAction}
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupViewItemsAction
import farjs.filelist.popups.ViewItemsPopup._
import farjs.ui.popup.StatusPopupProps
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class ViewItemsPopupSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  ViewItemsPopup.statusPopupComp = mockUiComponent("StatusPopup")

  it should "dispatch action with calculated items sizes" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 1", size = 10)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true, selectedNames = Set("dir 1", "file 1")),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 123)
      ))
      p.future
    }
    val renderer = createTestRenderer(<(ViewItemsPopup())(^.wrapped := props)())
    
    eventually {
      TestRenderer.act { () =>
        renderer.update(<(ViewItemsPopup())(^.wrapped := props)())
      }
      val popup = findComponentProps(renderer.root, statusPopupComp)
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      var resAction: FileListItemsViewedAction = null

      //then
      dispatch.expects(*).onCall { action: Any =>
        inside(action) { case action: FileListItemsViewedAction =>
          resAction = action
        }
      }

      //when
      p.success(true)

      //then
      eventually {
        resAction should not be null
        resAction shouldBe FileListItemsViewedAction(isRight = false, Map(
          "dir 1" -> 123,
          "file 1" -> 10
        ))
      }
    }
  }

  it should "handle cancel action and hide StatusPopup when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)
    
    val renderer = createTestRenderer(<(ViewItemsPopup())(^.wrapped := props)())
    val popup = findComponentProps(renderer.root, statusPopupComp)
    val action = FileListPopupViewItemsAction(show = false)

    //then
    dispatch.expects(action)

    //when
    popup.onClose()
    
    //then
    TestRenderer.act { () =>
      renderer.update(<(ViewItemsPopup())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))())
    }

    renderer.root.children.toList should be (empty)
    
    p.success(false)
    Succeeded
  }

  it should "dispatch actions when failure" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)
    
    val renderer = createTestRenderer(<(ViewItemsPopup())(^.wrapped := props)())
    findComponentProps(renderer.root, statusPopupComp)
    val action = FileListPopupViewItemsAction(show = false)
    var resultF: Future[_] = null

    //then
    dispatch.expects(action)
    dispatch.expects(*).onCall { action: Any =>
      inside(action) { case action: FileListTaskAction =>
        resultF = action.task.future
      }
    }

    //when
    p.failure(new Exception("test error"))
    
    //then
    eventually {
      resultF should not be null
    }.flatMap(_ => resultF.failed).map { _ =>
      Succeeded
    }
  }

  it should "render empty component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState())

    //when
    val result = createTestRenderer(<(ViewItemsPopup())(^.wrapped := props)()).root

    //then
    result.children.toList should be (empty)
  }
  
  it should "render StatusPopup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val props = FileListPopupsProps(dispatch, actions, FileListsState(
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val action = FileListItemsViewedAction(isRight = false, Map.empty)

    //then
    dispatch.expects(action)

    //when
    val result = testRender(<(ViewItemsPopup())(^.wrapped := props)())

    //then
    assertTestComponent(result, statusPopupComp) {
      case StatusPopupProps(text, title, closable, _) =>
        text shouldBe "Scanning the folder\n"
        title shouldBe "View"
        closable shouldBe true
    }
  }
}
