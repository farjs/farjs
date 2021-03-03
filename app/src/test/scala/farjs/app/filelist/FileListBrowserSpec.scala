package farjs.app.filelist

import farjs.app.filelist.FileListBrowser._
import farjs.app.filelist.FileListBrowserSpec._
import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class FileListBrowserSpec extends TestSpec with TestRendererUtils {
  
  FileListBrowser.fileListPanelComp = () => "FileListPanel".asInstanceOf[ReactClass]
  FileListBrowser.bottomMenuComp = () => "BottomMenu".asInstanceOf[ReactClass]

  it should "swap the panels when Ctrl+U" in {
    //given
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val leftState = mock[FileListState]
    val rightState = mock[FileListState]
    val data = mock[FileListsStateDef]
    (data.left _).expects().returning(leftState).twice()
    (data.right _).expects().returning(rightState).twice()
    
    val props = FileListBrowserProps(dispatch, actions, data)
    val screenMock = mock[BlessedScreenMock]
    val boxMock = mock[BlessedElementMock]
    var keyListener: js.Function2[js.Object, KeyboardKey, Unit] = null
    
    (boxMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *).onCall { (keys, listener) =>
      keys.toList shouldBe List("C-u")
      keyListener = listener
    }
    val renderer = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    })
    val List(leftPanel, rightPanel) = findProps(renderer.root, fileListPanelComp)
    leftPanel.state shouldBe leftState
    rightPanel.state shouldBe rightState
    
    //then
    (screenMock.focusNext _).expects()

    //when
    TestRenderer.act { () =>
      keyListener(null, null)
    }

    //then
    val List(newLeftPanel, newRightPanel) = findProps(renderer.root, fileListPanelComp)
    newLeftPanel.state shouldBe rightState
    newRightPanel.state shouldBe leftState
  }

  it should "render component" in {
    //given
    val dispatch = mock[Dispatch]
    val actions = mock[FileListActions]
    val leftState = mock[FileListState]
    val rightState = mock[FileListState]
    val data = mock[FileListsStateDef]
    (data.left _).expects().returning(leftState)
    (data.right _).expects().returning(rightState)
    val props = FileListBrowserProps(dispatch, actions, data)

    val screenMock = mock[BlessedScreenMock]
    val boxMock = mock[BlessedElementMock]
    (boxMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.key _).expects(*, *)

    //when
    val result = createTestRenderer(<(FileListBrowser())(^.wrapped := props)(), { el =>
      if (el.`type` == <.box.name.asInstanceOf[js.Any]) boxMock.asInstanceOf[js.Any]
      else null
    }).root

    //then
    inside(result.children.toList) { case List(left, right, menu) =>
      assertNativeComponent(left, <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1"
      )(), { case List(panel) =>
        assertTestComponent(panel, fileListPanelComp) {
          case FileListPanelProps(resDispatch, resActions, state) =>
            resDispatch should be theSameInstanceAs dispatch
            resActions should be theSameInstanceAs actions
            state shouldBe leftState
        }
      })
      assertNativeComponent(right, <.box(
        ^.rbWidth := "50%",
        ^.rbHeight := "100%-1",
        ^.rbLeft := "50%"
      )(), { case List(panel) =>
        assertTestComponent(panel, fileListPanelComp) {
          case FileListPanelProps(resDispatch, resActions, state) =>
            resDispatch should be theSameInstanceAs dispatch
            resActions should be theSameInstanceAs actions
            state shouldBe rightState
        }
      })

      assertNativeComponent(menu,
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())()()
        )
      )
    }
  }
}

object FileListBrowserSpec {

  @JSExportAll
  trait BlessedScreenMock {

    def key(keys: js.Array[String], onKey: js.Function2[js.Object, KeyboardKey, Unit]): Unit
    def focusNext(): Unit
  }

  @JSExportAll
  trait BlessedElementMock {

    def screen: BlessedScreen
  }
}
