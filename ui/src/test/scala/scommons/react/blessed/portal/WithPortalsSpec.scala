package scommons.react.blessed.portal

import java.util.concurrent.atomic.AtomicReference
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.test._

import scala.scalajs.js.Dynamic.literal

class WithPortalsSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

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
    val focused1 = literal()
    val screenMock = literal("focused" -> focused1)
    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
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
    
    val focused2 = literal()
    screenMock.focused = focused2
    
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
    val screenMock = literal("focused" -> null)
    
    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
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
    val (portalCtx0, portalComp0) = getPortalCtxHook("portal content 0")
    val (portalCtx1, portalComp1) = getPortalCtxHook("portal content 1")
    val (portalCtx2, portalComp2) = getPortalCtxHook("portal content 2")
    val (portalCtx3, portalComp3) = getPortalCtxHook("portal content 3")
    val renderMock = mockFunction[Unit]
    val screenMock = literal("focused" -> null, "render" -> renderMock)
    
    var renderNum = 0
    renderMock.expects().onCall { () =>
      renderNum += 1
    }.repeated(4)

    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root
    portalsCtx.get.onRender(0, <(portalComp0).empty)

    //given & when
    val focus1Mock = mockFunction[Unit]
    val focused1 = literal("focus" -> focus1Mock)
    screenMock.focused = focused1
    focus1Mock.expects()
    portalsCtx.get.onRender(1, <(portalComp1).empty)

    //given & when
    val focus2Mock = mockFunction[Unit]
    val focused2 = literal("focus" -> focus2Mock)
    screenMock.focused = focused2
    focus2Mock.expects().never()
    portalsCtx.get.onRender(2, <(portalComp2).empty)

    //given & when
    val focus3Mock = mockFunction[Unit]
    val focused3 = literal("focus" -> focus3Mock)
    screenMock.focused = focused3
    focus3Mock.expects()
    portalsCtx.get.onRender(3, <(portalComp3).empty)

    //then
    portalCtx0.get.isActive shouldBe false
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe false
    portalCtx3.get.isActive shouldBe true

    //when & then
    portalsCtx.get.onRemove(3)
    portalCtx1.get.isActive shouldBe false
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal0, portal1, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal0.`type` shouldBe portalComp0
      portal1.`type` shouldBe portalComp1
      portal2.`type` shouldBe portalComp2
    }
    
    //when & then
    portalsCtx.get.onRemove(1)
    portalCtx2.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal0, portal2) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal0.`type` shouldBe portalComp0
      portal2.`type` shouldBe portalComp2
    }
    
    //when & then
    portalsCtx.get.onRemove(2)
    portalCtx0.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal0) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal0.`type` shouldBe portalComp0
    }
    
    //when & then
    portalsCtx.get.onRemove(0)
    inside(root.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
    }
    
    eventually {
      renderNum shouldBe 4
    }
  }
  
  it should "do nothing if portal not found when onRemove" in {
    //given
    val (portalsCtx, portalsComp) = getPortalsCtxHook
    val renderMock = mockFunction[Unit]
    val screenMock = literal("render" -> renderMock)
    renderMock.expects().never()

    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root

    //when & then
    portalsCtx.get.onRemove(123)
    inside(root.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
    }
  }

  it should "render nested portals" in {
    //given
    val renderMock = mockFunction[Unit]
    val screenMock = literal("render" -> renderMock)
    renderMock.expects().never()

    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
      <(Portal())()(
        <.>()("parent portal"),
        <(Portal())()(
          <.>()("nested portal")
        )
      ),
      <.>()("some other children")
    )).root

    //when & then
    inside(root.children.toList) { case List(p1, childrenContent, parentPortal, p2, nestedPortal) =>
      p1.`type` shouldBe Portal()
      childrenContent shouldBe "some other children"
      parentPortal shouldBe "parent portal"
      p2.`type` shouldBe Portal()
      nestedPortal shouldBe "nested portal"
    }
  }
}
