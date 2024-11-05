package farjs.app.filelist

import farjs.filelist.history.HistoryProvider
import farjs.fs.FSServices
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.scalajs.js

class FileListRootSpec extends TestSpec with TestRendererUtils {

  private val withPortalsComp = "WithPortals".asInstanceOf[ReactClass]

  it should "render component with contexts" in {
    //given
    val (historyProviderCtx, fsCtx, servicesComp) = getServicesCtxHook
    FileListRoot.fileListComp = servicesComp
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val module = mock[FileListModule]
    val rootComp = new FileListRoot(dispatch, module, withPortalsComp)
    
    //when
    val result = createTestRenderer(<(rootComp())()(
      "test_child"
    )).root
    
    //then
    historyProviderCtx.get() shouldBe module.historyProvider
    fsCtx.get() shouldBe module.fsServices
    assertComponents(result.children, List(
      <(withPortalsComp)()(
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
    AtomicReference[HistoryProvider], AtomicReference[FSServices], ReactClass
    ) = {

    val historyProviderRef = new AtomicReference[HistoryProvider](null)
    val fsRef = new AtomicReference[FSServices](null)
    (historyProviderRef, fsRef, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        historyProviderRef.set(useContext(HistoryProvider.Context))
        fsRef.set(useContext(FSServices.Context))
        <.>()()
      }
    }.apply())
  }
}
