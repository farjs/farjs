package scommons.react.blessed.portal

import java.util.concurrent.atomic.AtomicReference

import scommons.react._
import scommons.react.hooks._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

class WithPortalsSpec extends TestSpec
  with TestRendererUtils
  with ShallowRendererUtils {

  private def getPortalsCtxHook: (AtomicReference[WithPortalsContext], ReactClass) = {
    val ref = new AtomicReference[WithPortalsContext](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(WithPortals.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
  
  private def getPortalCtxHook(content: String): (AtomicReference[PortalContext], ReactClass) = {
    val ref = new AtomicReference[PortalContext](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(Portal.Context)
        ref.set(ctx)
        <.>()(content)
      }
    }.apply())
  }
  
  it should "add new portals when onRender" in {
    //given
    val (portalsCtx, portalsComp) = getPortalsCtxHook
    val (portalCtx1, portalComp1) = getPortalCtxHook("portal content 1")
    val (portalCtx2, portalComp2) = getPortalCtxHook("portal content 2")
    val root = createTestRenderer(<(WithPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root

    //when & then
    portalsCtx.get.onRender(1, <(portalComp1).empty)
    portalCtx1.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe portalComp1
    }
    
    //when & then
    portalsCtx.get.onRender(2, <(portalComp2).empty)
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe portalComp1
      portal2.`type` shouldBe portalComp2
    }
  }
  
  it should "update existing portals when onRender" in {
    //given
    val (portalsCtx, portalsComp) = getPortalsCtxHook
    val (portalCtx1, portalComp1) = getPortalCtxHook("portal content 1")
    val (portalCtx2, portalComp2) = getPortalCtxHook("portal content 2")
    val root = createTestRenderer(<(WithPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root
    portalsCtx.get.onRender(1, <(portalComp1).empty)
    portalsCtx.get.onRender(2, <(portalComp2).empty)
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe true
    val (updatedCtx1, updatedComp1) = getPortalCtxHook("updated content 1")
    val (updatedCtx2, updatedComp2) = getPortalCtxHook("updated content 2")

    //when & then
    portalsCtx.get.onRender(1, <(updatedComp1).empty)
    updatedCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe updatedComp1
      portal2.`type` shouldBe portalComp2
    }
    
    //when & then
    portalsCtx.get.onRender(2, <(updatedComp2).empty)
    updatedCtx1.get.isActive shouldBe false
    updatedCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe updatedComp1
      portal2.`type` shouldBe updatedComp2
    }
  }
  
  it should "remove portals when onRemove" in {
    //given
    val (portalsCtx, portalsComp) = getPortalsCtxHook
    val (portalCtx1, portalComp1) = getPortalCtxHook("portal content 1")
    val (portalCtx2, portalComp2) = getPortalCtxHook("portal content 2")
    val (portalCtx3, portalComp3) = getPortalCtxHook("portal content 3")
    val root = createTestRenderer(<(WithPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root
    portalsCtx.get.onRender(1, <(portalComp1).empty)
    portalsCtx.get.onRender(2, <(portalComp2).empty)
    portalsCtx.get.onRender(3, <(portalComp3).empty)
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe false
    portalCtx3.get.isActive shouldBe true

    //when & then
    portalsCtx.get.onRemove(3)
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe portalComp1
      portal2.`type` shouldBe portalComp2
    }
    
    //when & then
    portalsCtx.get.onRemove(1)
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal2.`type` shouldBe portalComp2
    }
    
    //when & then
    portalsCtx.get.onRemove(2)
    inside(root.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
    }
  }
  
  it should "set key and Portal.Context when renderPortal" in {
    //given
    val id = 123
    val isActive = true
    val content = <.>()("some portal content")

    //when
    val result = WithPortals.renderPortal(id, content, isActive)

    //then
    assertPortal(result, id, content, isActive)
  }
  
  private def assertPortal(result: ReactElement, id: Int, content: ReactElement, isActive: Boolean): Unit = {
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        result
      }
    }
    
    val resInstance = shallowRender(<(wrapper()).empty)
    assertNativeComponent(resInstance, <.>(^.key := s"$id")(), { children: List[ShallowInstance] =>
      val List(provider) = children
      
      val ctx = provider.props.selectDynamic("value").asInstanceOf[PortalContext]
      ctx shouldBe PortalContext(isActive)
      
      assertNativeComponent(provider, <(Portal.Context.Provider)()(), { children: List[ShallowInstance] =>
        val List(resContent) = children
        resContent shouldBe content
      })
    })
  }
}
