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
  
  it should "call onAdd/onRemove when mount/un-mount" in {
    //given
    val onAdd = mockFunction[ReactElement, Int]
    val onRemove = mockFunction[Int, Unit]
    val portalId = 123
    val content = <.>()()
    
    //then
    onAdd.expects(content).returning(portalId)
    onRemove.expects(portalId)
    
    //when
    val renderer = createTestRenderer {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onAdd, onRemove))(
        Portal.create(content)
      )
    }
    
    //when
    renderer.unmount()
  }
  
  it should "call onAdd/onRemove if different content when update" in {
    //given
    val onAdd = mockFunction[ReactElement, Int]
    val onRemove = mockFunction[Int, Unit]
    val onAddJs: js.Function1[ReactElement, Int] = onAdd
    val onRemoveJs: js.Function1[Int, Unit] = onRemove
    val portalId1 = 1
    val portalId2 = 1
    val content1 = <.>()()
    val content2 = <.>()()
    
    onAdd.expects(content1).returning(portalId1)
    val renderer = createTestRenderer {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onAddJs, onRemoveJs))(
        Portal.create(content1)
      )
    }

    //then
    onRemove.expects(portalId1)
    onAdd.expects(content2).returning(portalId2)

    //when
    renderer.update {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onAddJs, onRemoveJs))(
        Portal.create(content2)
      )
    }
    
    //cleanup
    onRemove.expects(portalId2)
    renderer.unmount()
  }
  
  it should "not call onAdd/onRemove if the same content when update" in {
    //given
    val onAdd = mockFunction[ReactElement, Int]
    val onRemove = mockFunction[Int, Unit]
    val onAddJs: js.Function1[ReactElement, Int] = onAdd
    val onRemoveJs: js.Function1[Int, Unit] = onRemove
    val portalId = 123
    val content = <.>()()
    
    //then
    onAdd.expects(content).returning(portalId).once()
    onRemove.expects(portalId).once()
    
    val renderer = createTestRenderer {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onAddJs, onRemoveJs))(
        Portal.create(content)
      )
    }

    //when
    renderer.update {
      <(Portal.Context.Provider)(^.contextValue := PortalContext(onAddJs, onRemoveJs))(
        Portal.create(content)
      )
    }
    
    //cleanup
    renderer.unmount()
  }
}
