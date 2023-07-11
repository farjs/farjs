package farjs.viewer.quickview

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.{TextAlign, TextLineProps}
import farjs.viewer.quickview.QuickViewDir._
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

class QuickViewDirSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  QuickViewDir.statusPopupComp = mockUiComponent("StatusPopup")
  QuickViewDir.textLineComp = "TextLine".asInstanceOf[ReactClass]

  private val currComp = "QuickViewPanel".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, Seq[FileListItem], (String, Seq[FileListItem]) => Boolean, Future[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "update params with calculated stats when item name changes" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams(parent = currDir.path)))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 122),
        FileListItem("file 1", size = 1)
      ))
      p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    
    eventually {
      val popup = findComponentProps(renderer.root, statusPopupComp)
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      p.success(true)

      //then
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path, 1, 2, 123))
        findProps(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "update params with calculated stats when curr path changes" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams(name = currItem.name)))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 122),
        FileListItem("file 1", size = 1)
      ))
      p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    
    eventually {
      val popup = findComponentProps(renderer.root, statusPopupComp)
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      p.success(true)

      //then
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path, 1, 2, 123))
        findProps(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "handle cancel action and hide StatusPopup when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    var onNextDirFn: (String, Seq[FileListItem]) => Boolean = null
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDirFn = onNextDir
      p.future
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    val popup = findComponentProps(renderer.root, statusPopupComp)

    eventually {
      val popup = findComponentProps(renderer.root, statusPopupComp)
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      popup.onClose()
      val result = onNextDirFn("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 122),
        FileListItem("file 1", size = 1)
      ))
      p.success(result)

      //then
      result shouldBe false
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path))
        findProps(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "dispatch action when failure" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)

    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    findProps(renderer.root, statusPopupComp) should not be empty

    //then
    var resultF: Future[_] = null
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
      findProps(renderer.root, statusPopupComp) should be (empty)
    }
  }

  it should "render component with existing params" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    val params = QuickViewParams(currItem.name, currDir.path, 1, 2, 3)
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(params))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)

    //when
    val result = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)())).root

    //then
    assertQuickViewDir(result.children, props, params)
  }

  private def assertQuickViewDir(children: js.Array[TestInstance],
                                 props: QuickViewDirProps,
                                 params: QuickViewParams): Assertion = {
    
    val theme = FileListTheme.defaultTheme.fileList

    assertComponents(children, List(
      <.text(
        ^.rbLeft := 2,
        ^.rbTop := 2,
        ^.rbStyle := theme.regularItem,
        ^.content :=
          """Folder
            |
            |Contains:
            |
            |Folders
            |Files
            |Files size""".stripMargin
      )(),

      <(textLineComp)(^.assertPlain[TextLineProps](inside(_) {
        case TextLineProps(align, left, top, resWidth, text, style, focused, padding) =>
          align shouldBe TextAlign.left
          left shouldBe 12
          top shouldBe 2
          resWidth shouldBe (props.width - 14)
          text shouldBe s""""${props.currItem.name}""""
          style shouldBe theme.regularItem
          focused shouldBe js.undefined
          padding shouldBe 0
      }))(),

      <.text(
        ^.rbLeft := 15,
        ^.rbTop := 6,
        ^.rbStyle := theme.selectedItem,
        ^.content :=
          f"""${params.folders}%,.0f
             |${params.files}%,.0f
             |${params.filesSize}%,.0f""".stripMargin
      )()
    ))
  }
}
