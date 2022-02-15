package farjs.filelist.popups

import farjs.filelist.FileListActions.{FileListItemsViewedAction, FileListTaskAction}
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupViewItemsAction
import farjs.filelist.popups.ViewItemsPopup._
import farjs.filelist.stack.WithPanelStacksSpec.withContext
import farjs.filelist.stack._
import farjs.ui.popup.StatusPopupProps
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class ViewItemsPopupSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  ViewItemsPopup.statusPopupComp = mockUiComponent("StatusPopup")

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean, Future[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "dispatch action with calculated items sizes" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 1", size = 10)
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(),
      right = FileListState(currDir = currDir, isRight = true, isActive = true, selectedNames = Set("dir 1", "file 1")),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 123)
      ))
      p.future
    }
    val leftStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val rightStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.right))
    ), null)
    val renderer = createTestRenderer(
      withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack)
    )
    
    eventually {
      TestRenderer.act { () =>
        renderer.update(withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack))
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
        resAction shouldBe FileListItemsViewedAction(isRight = true, Map(
          "dir 1" -> 123,
          "file 1" -> 10
        ))
      }
    }
  }

  it should "handle cancel action and hide StatusPopup when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)
    
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val renderer = createTestRenderer(
      withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack)
    )
    val popup = findComponentProps(renderer.root, statusPopupComp)
    val action = FileListPopupViewItemsAction(show = false)

    //then
    dispatch.expects(action)

    //when
    popup.onClose()
    
    //then
    TestRenderer.act { () =>
      renderer.update(withContext(<(ViewItemsPopup())(^.wrapped := props.copy(
        data = FileListsState(popups = FileListPopupsState())
      ))(), leftStack, rightStack))
    }

    renderer.root.children.toList should be (empty)
    
    p.success(false)
    Succeeded
  }

  it should "dispatch actions when failure" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currDir = FileListDir("/folder", isRoot = false, List(
      FileListItem("dir 1", isDir = true)
    ))
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      left = FileListState(currDir = currDir, isActive = true),
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)
    
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)
    val renderer = createTestRenderer(
      withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack)
    )
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
    val actions = new Actions
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState())
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //when
    val result = createTestRenderer(
      withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack)
    ).root

    //then
    result.children.toList should be (empty)
  }
  
  it should "render StatusPopup component" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val props = FileListPopupsProps(dispatch, actions.actions, FileListsState(
      popups = FileListPopupsState(showViewItemsPopup = true)
    ))
    val action = FileListItemsViewedAction(isRight = false, Map.empty)
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions.actions), Some(props.data.left))
    ), null)
    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("otherComp".asInstanceOf[ReactClass], None, None, None)
    ), null)

    //then
    dispatch.expects(action)

    //when
    val result = testRender(
      withContext(<(ViewItemsPopup())(^.wrapped := props)(), leftStack, rightStack)
    )

    //then
    assertTestComponent(result, statusPopupComp) {
      case StatusPopupProps(text, title, closable, _) =>
        text shouldBe "Scanning the folder\n"
        title shouldBe "View"
        closable shouldBe true
    }
  }
}
