package scommons.react.blessed.portal

import java.util.concurrent.atomic.AtomicReference

import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.WithPortalsSpec._
import scommons.react.hooks._
import scommons.react.test._

import scala.scalajs.js.annotation.JSExportAll

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
    val screenMock = mock[BlessedScreenMock]
    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root

    val focused1 = mock[BlessedElementMock]
    (screenMock.focused _).expects().returning(focused1.asInstanceOf[BlessedElement])
    
    //when & then
    portalsCtx.get.onRender(1, <(portalComp1).empty)
    portalCtx1.get.isActive shouldBe true
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1) =>
      resCtxHook.`type` shouldBe portalsComp
      otherContent shouldBe "some other content"
      portal1.`type` shouldBe portalComp1
    }
    
    val focused2 = mock[BlessedElementMock]
    (screenMock.focused _).expects().returning(focused2.asInstanceOf[BlessedElement])
    
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
    val screenMock = mock[BlessedScreenMock]
    (screenMock.focused _).expects().returning(null).twice()
    
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
    val screenMock = mock[BlessedScreenMock]
    val focused1 = mock[BlessedElementMock]
    val focused2 = mock[BlessedElementMock]
    val focused3 = mock[BlessedElementMock]
    (screenMock.focused _).expects().returning(null)
    (screenMock.focused _).expects().returning(focused1.asInstanceOf[BlessedElement])
    (screenMock.focused _).expects().returning(focused2.asInstanceOf[BlessedElement])
    (screenMock.focused _).expects().returning(focused3.asInstanceOf[BlessedElement])
    
    var renderNum = 0
    (screenMock.render _).expects().onCall { () =>
      renderNum += 1
    }.repeated(4)

    //then
    (focused3.focus _).expects()
    (focused1.focus _).expects()
    (focused2.focus _).expects().never()
    
    val withPortals = new WithPortals(screenMock.asInstanceOf[BlessedScreen])
    val root = createTestRenderer(<(withPortals())()(
      <(portalsComp).empty,
      <.>()("some other content")
    )).root
    portalsCtx.get.onRender(0, <(portalComp0).empty)
    portalsCtx.get.onRender(1, <(portalComp1).empty)
    portalsCtx.get.onRender(2, <(portalComp2).empty)
    portalsCtx.get.onRender(3, <(portalComp3).empty)
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
    val screenMock = mock[BlessedScreenMock]
    (screenMock.render _).expects().never()

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
}

object WithPortalsSpec {

  @JSExportAll
  trait BlessedScreenMock {

    def focused: BlessedElement
    def render(): Unit
  }
  
  @JSExportAll
  trait BlessedElementMock {

    def focus(): Unit
  }
}
