package scommons.react.blessed.portal

import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class PortalSpec extends TestSpec with TestRendererUtils {

  it should "fail if no WithPortals.Context when render" in {
    //given
    val portal = <(Portal())()(<.>()())
    
    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = { _: js.Any =>
    }

    //when
    val result = testRender(<(TestErrorBoundary())()(
      portal
    ))
    
    //then
    js.Dynamic.global.console.error = savedConsoleError
    
    assertNativeComponent(result,
      <.div()(
        "Error: WithPortals.Context is not found." +
          "\nPlease, make sure you use WithPortals and not creating nested portals."
      )
    )
  }
  
  it should "call onRender/onRemove when mount/un-mount" in {
    //given
    val onRender = mockFunction[Int, ReactElement, Unit]
    val onRemove = mockFunction[Int, Unit]
    val portalId = Portal.nextPortalId + 1
    val content = <.>()()
    
    //then
    onRender.expects(portalId, content)
    onRemove.expects(portalId)
    
    //when
    val renderer = createTestRenderer {
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRender, onRemove))(
        <(Portal())()(content)
      )
    }
    
    //when
    renderer.unmount()
  }
  
  it should "call onRender if different content when update" in {
    //given
    val onRender = mockFunction[Int, ReactElement, Unit]
    val onRemove = mockFunction[Int, Unit]
    val onRenderJs: js.Function2[Int, ReactElement, Unit] = onRender
    val onRemoveJs: js.Function1[Int, Unit] = onRemove
    val portalId = Portal.nextPortalId + 1
    val content1 = <.>()()
    val content2 = <.>()()

    onRender.expects(portalId, content1)
    val renderer = createTestRenderer {
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRenderJs, onRemoveJs))(
        <(Portal())()(content1)
      )
    }

    //then
    onRender.expects(portalId, content2)

    //when
    renderer.update {
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRenderJs, onRemoveJs))(
        <(Portal())()(content2)
      )
    }
    
    //cleanup
    onRemove.expects(portalId)
    renderer.unmount()
  }
  
  it should "not call onRender if the same content when update" in {
    //given
    val onRender = mockFunction[Int, ReactElement, Unit]
    val onRemove = mockFunction[Int, Unit]
    val onRenderJs: js.Function2[Int, ReactElement, Unit] = onRender
    val onRemoveJs: js.Function1[Int, Unit] = onRemove
    val portalId = Portal.nextPortalId + 1
    val content = <.>()()
    
    //then
    onRender.expects(portalId, content).once()
    
    val renderer = createTestRenderer {
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRenderJs, onRemoveJs))(
        <(Portal())()(content)
      )
    }

    //when
    renderer.update {
      <(WithPortals.Context.Provider)(^.contextValue := WithPortalsContext(onRenderJs, onRemoveJs))(
        <(Portal())()(content)
      )
    }
    
    //cleanup
    onRemove.expects(portalId)
    renderer.unmount()
  }
}
