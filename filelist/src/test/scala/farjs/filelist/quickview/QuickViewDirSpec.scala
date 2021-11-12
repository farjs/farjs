package farjs.filelist.quickview

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.quickview.QuickViewDir._
import farjs.filelist.stack.PanelStack
import farjs.filelist.stack.PanelStack.StackItem
import farjs.ui.theme.Theme
import farjs.ui.{TextLine, TextLineProps}
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
  QuickViewDir.textLineComp = mockUiComponent("TextLine")

  private val currComp = "QuickViewPanel".asInstanceOf[ReactClass]

  it should "update params with calculated stats" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[StackItem](
      (currComp, QuickViewParams().asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })
    val props = QuickViewDirProps(dispatch, actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDir("/path", List(
        FileListItem("dir 2", isDir = true),
        FileListItem("file 2", size = 122),
        FileListItem("file 1", size = 1)
      ))
      p.future
    }
    val renderer = createTestRenderer(<(QuickViewDir())(^.wrapped := props)())
    
    eventually {
      val popup = findComponentProps(renderer.root, statusPopupComp)
      popup.text shouldBe "Scanning the folder\ndir 1"
    }.flatMap { _ =>
      //when
      p.success(true)

      //then
      eventually {
        stackState.head._2.asInstanceOf[QuickViewParams] shouldBe QuickViewParams("dir 1", 1, 2, 123)
        findProps(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "handle cancel action and hide StatusPopup when onClose" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[StackItem](
      (currComp, QuickViewParams().asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })
    val props = QuickViewDirProps(dispatch, actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    var onNextDirFn: (String, Seq[FileListItem]) => Boolean = null
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).onCall { (_, _, onNextDir) =>
      onNextDirFn = onNextDir
      p.future
    }
    val renderer = createTestRenderer(<(QuickViewDir())(^.wrapped := props)())
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
        stackState.head._2.asInstanceOf[QuickViewParams] shouldBe QuickViewParams("dir 1")
        findProps(renderer.root, statusPopupComp) should be (empty)
      }
    }
  }

  it should "dispatch action when failure" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = mock[FileListActions]
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    var stackState = List[StackItem](
      (currComp, QuickViewParams().asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })
    val props = QuickViewDirProps(dispatch, actions, FileListState(currDir = currDir), stack, 25, currItem)
    val p = Promise[Boolean]()
    (actions.scanDirs _).expects(currDir.path, Seq(currDir.items.head), *).returning(p.future)

    val renderer = createTestRenderer(<(QuickViewDir())(^.wrapped := props)())
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
    val actions = mock[FileListActions]
    val currItem = FileListItem("dir 1", isDir = true)
    val currDir = FileListDir("/folder", isRoot = false, List(
      currItem,
      FileListItem("file 1", size = 10)
    ))
    val params = QuickViewParams(currItem.name, 1, 2, 3)
    var stackState = List[StackItem](
      (currComp, params.asInstanceOf[js.Any])
    )
    val stack = new PanelStack(stackState.headOption, { f: js.Function1[List[StackItem], List[StackItem]] =>
      stackState = f(stackState)
    })
    val props = QuickViewDirProps(dispatch, actions, FileListState(currDir = currDir), stack, 25, currItem)

    //when
    val result = createTestRenderer(<(QuickViewDir())(^.wrapped := props)()).root

    //then
    assertQuickViewDir(result.children.toList, props, params)
  }

  private def assertQuickViewDir(children: List[TestInstance],
                                 props: QuickViewDirProps,
                                 params: QuickViewParams): Assertion = {
    
    val theme = Theme.current.fileList

    def assertComponents(content: TestInstance,
                         item: TestInstance,
                         stats: TestInstance): Assertion = {

      assertNativeComponent(content,
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
        )()
      )

      assertTestComponent(item, textLineComp) {
        case TextLineProps(align, pos, resWidth, text, style, focused, padding) =>
          align shouldBe TextLine.Left
          pos shouldBe 12 -> 2
          resWidth shouldBe (props.width - 14)
          text shouldBe s""""${props.currItem.name}""""
          style shouldBe theme.regularItem
          focused shouldBe false
          padding shouldBe 0
      }

      assertNativeComponent(stats,
        <.text(
          ^.rbLeft := 15,
          ^.rbTop := 6,
          ^.rbStyle := theme.selectedItem,
          ^.content :=
            f"""${params.folders}%,.0f
               |${params.files}%,.0f
               |${params.filesSize}%,.0f""".stripMargin
        )()
      )
    }

    inside(children) {
      case List(content, item, stats) => assertComponents(content, item, stats)
    }
  }
}
