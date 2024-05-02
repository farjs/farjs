package farjs.viewer.quickview

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import farjs.filelist.theme.FileListTheme
import farjs.filelist.theme.FileListThemeSpec.withThemeContext
import farjs.ui.popup.StatusPopupProps
import farjs.ui.task.TaskAction
import farjs.ui.{TextAlign, TextLineProps}
import farjs.viewer.quickview.QuickViewDir._
import org.scalatest.Assertion
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class QuickViewDirSpec extends AsyncTestSpec with BaseTestSpec
  with TestRendererUtils {

  QuickViewDir.statusPopupComp = "StatusPopup".asInstanceOf[ReactClass]
  QuickViewDir.textLineComp = "TextLine".asInstanceOf[ReactClass]

  private val currComp = "QuickViewPanel".asInstanceOf[ReactClass]

  //noinspection TypeAnnotation
  class Actions {
    val scanDirs = mockFunction[String, js.Array[FileListItem], js.Function2[String, js.Array[FileListItem], Boolean], js.Promise[Boolean]]

    val actions = new MockFileListActions(
      scanDirsMock = scanDirs
    )
  }

  it should "update params with calculated stats when item name changes" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      currItem,
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams(parent = currDir.path)))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, *, *).onCall { (_, resItems, onNextDir) =>
      resItems.toList shouldBe Seq(currDir.items.head)
      onNextDir("/path", js.Array(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 122),
        FileListItem.copy(FileListItem("file 1"))(size = 1)
      ))
      p.future.toJSPromise
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    
    eventually {
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      p.success(true)

      //then
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path, 1, 2, 123))
        findComponents(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "update params with calculated stats when curr path changes" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      currItem,
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams(name = currItem.name)))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, *, *).onCall { (_, resItems, onNextDir) =>
      resItems.toList shouldBe Seq(currDir.items.head)
      onNextDir("/path", js.Array(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 122),
        FileListItem.copy(FileListItem("file 1"))(size = 1)
      ))
      p.future.toJSPromise
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    
    eventually {
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      p.success(true)

      //then
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path, 1, 2, 123))
        findComponents(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "handle cancel action and hide StatusPopup when onClose" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      currItem,
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    var onNextDirFn: js.Function2[String, js.Array[FileListItem], Boolean] = null
    actions.scanDirs.expects(currDir.path, *, *).onCall { (_, resItems, onNextDir) =>
      resItems.toList shouldBe Seq(currDir.items.head)
      onNextDirFn = onNextDir
      p.future.toJSPromise
    }
    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    val popup = inside(findComponents(renderer.root, statusPopupComp)) {
      case List(p) => p.props.asInstanceOf[StatusPopupProps]
    }

    eventually {
      val popup = inside(findComponents(renderer.root, statusPopupComp)) {
        case List(p) => p.props.asInstanceOf[StatusPopupProps]
      }
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      popup.onClose.foreach(_.apply())
      val result = onNextDirFn("/path", js.Array(
        FileListItem("dir 2", isDir = true),
        FileListItem.copy(FileListItem("file 2"))(size = 122),
        FileListItem.copy(FileListItem("file 1"))(size = 1)
      ))
      p.success(result)

      //then
      result shouldBe false
      eventually {
        stackState.head.state shouldBe Some(QuickViewParams("dir 1", currDir.path))
        findComponents(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "dispatch action when failure" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      currItem,
      FileListItem.copy(FileListItem("file 1"))(size = 10)
    ))
    var stackState = List[PanelStackItem[QuickViewParams]](
      PanelStackItem(currComp, None, None, Some(QuickViewParams()))
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[QuickViewParams]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    val props = QuickViewDirProps(dispatch, actions.actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    actions.scanDirs.expects(currDir.path, *, *).onCall { (_, resItems, _) =>
      resItems.toList shouldBe Seq(currDir.items.head)
      p.future.toJSPromise
    }

    val renderer = createTestRenderer(withThemeContext(<(QuickViewDir())(^.wrapped := props)()))
    findComponents(renderer.root, statusPopupComp) should not be empty

    //then
    var resultF: Future[_] = null
    dispatch.expects(*).onCall { action: Any =>
      inside(action.asInstanceOf[TaskAction]) { case action: TaskAction =>
        resultF = action.task.result.toFuture
      }
    }

    //when
    p.failure(new Exception("test error"))

    //then
    eventually {
      resultF should not be null
    }.flatMap(_ => resultF.failed).map { _ =>
      findComponents(renderer.root, statusPopupComp) should be (empty)
    }
  }

  it should "render component with existing params" in {
    //given
    val dispatch = mockFunction[js.Any, Unit]
    val actions = new Actions
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, js.Array(
      currItem,
      FileListItem.copy(FileListItem("file 1"))(size = 10)
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
