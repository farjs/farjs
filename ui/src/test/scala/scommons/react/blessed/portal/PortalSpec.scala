package scommons.react.blessed.portal

import scommons.react._
import scommons.react.test.TestSpec
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class PortalSpec extends TestSpec
  with TestRendererUtils
  with ShallowRendererUtils {

  it should "fail if no PortalContext when render" in {
    //given
    val portal = Portal.create(<.>()())
    
    //when
    val JavaScriptException(error) = the[JavaScriptException] thrownBy {
      shallowRender(portal)
    }
    
    //then
    s"$error" shouldBe {
      "Error: PortalContext is not specified, use WithPortals to wrap your root component"
    }
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
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onRender, onRemove))(
        Portal.create(content)
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
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onRenderJs, onRemoveJs))(
        Portal.create(content1)
      )
    }

    //then
    onRender.expects(portalId, content2)

    //when
    renderer.update {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onRenderJs, onRemoveJs))(
        Portal.create(content2)
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
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onRenderJs, onRemoveJs))(
        Portal.create(content)
      )
    }

    //when
    renderer.update {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onRenderJs, onRemoveJs))(
        Portal.create(content)
      )
    }
    
    //cleanup
    onRemove.expects(portalId)
    renderer.unmount()
  }
}
