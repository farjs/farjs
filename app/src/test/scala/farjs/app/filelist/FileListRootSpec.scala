package farjs.app.filelist

import farjs.filelist.FileListServices
import farjs.fs.FSServices
import farjs.text.TextServices
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference

class FileListRootSpec extends TestSpec with TestRendererUtils {

  private val withPortalsComp = mockUiComponent[Unit]("WithPortals")

  it should "render component with contexts" in {
    //given
    val (fileListCtx, fsCtx, textCtx, servicesComp) = getServicesCtxHook
    FileListRoot.fileListComp = servicesComp
    val dispatch = mockFunction[Any, Any]
    val module = mock[FileListModule]
    val rootComp = new FileListRoot(dispatch, module, withPortalsComp)
    
    //when
    val result = createTestRenderer(<(rootComp())()(
      "test_child"
    )).root
    
    //then
    fileListCtx.get() shouldBe module.fileListServices
    fsCtx.get() shouldBe module.fsServices
    textCtx.get() shouldBe module.textServices
    assertComponents(result.children, List(
      <(withPortalsComp())()(
        <(servicesComp)(^.assertWrapped(inside(_) {
          case FileListBrowserProps(resDispatch, isRightInitiallyActive, plugins) =>
            resDispatch should be theSameInstanceAs dispatch
            isRightInitiallyActive shouldBe false
            plugins should not be empty
        }))(),

        "test_child"
      )
    ))
  }

  private def getServicesCtxHook: (
    AtomicReference[FileListServices], AtomicReference[FSServices], AtomicReference[TextServices], ReactClass
    ) = {

    val fileListRef = new AtomicReference[FileListServices](null)
    val fsRef = new AtomicReference[FSServices](null)
    val textRef = new AtomicReference[TextServices](null)
    (fileListRef, fsRef, textRef, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        fileListRef.set(useContext(FileListServices.Context))
        fsRef.set(useContext(FSServices.Context))
        textRef.set(useContext(TextServices.Context))
        <.>()()
      }
    }.apply())
  }
}
