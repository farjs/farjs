package farjs.app.filelist

import farjs.filelist.FileListServices
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference

class FileListRootSpec extends TestSpec with TestRendererUtils {

  it should "render component with context provider" in {
    //given
    val (servicesCtx, servicesComp) = getServicesCtxHook
    FileListRoot.fileListComp = servicesComp
    val services = mock[FileListServices]
    val rootComp = new FileListRoot(services)
    
    //when
    val result = createTestRenderer(<(rootComp()).empty).root
    
    //then
    servicesCtx.get() shouldBe services
    assertComponents(result.children, List(
      <(servicesComp).empty
    ))
  }

  private def getServicesCtxHook: (AtomicReference[FileListServices], ReactClass) = {
    val ref = new AtomicReference[FileListServices](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(FileListServices.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}
