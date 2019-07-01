package scommons.react.blessed.portal

import scommons.react._
import scommons.react.blessed.portal.WithPortals._
import scommons.react.hooks._
import scommons.react.test.TestSpec
import scommons.react.test.util.TestRendererUtils

class WithPortalsSpec extends TestSpec
  with TestRendererUtils {

  it should "add new portals when onAdd" in {
    //given
    var ctx: PortalContext = null
    val ctxHook = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        ctx = useContext(Portal.Context)
        <.>()()
      }
    }
    val root = createTestRenderer(<(WithPortals())()(
      <(ctxHook()).empty,
      <.>()("some other content")
    )).root

    //when & then
    ctx.onAdd(<.>()("portal content 1"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "portal content 1"
    }
    
    //when & then
    ctx.onAdd(<.>()("portal content 2"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "portal content 1"
      portal2 shouldBe "portal content 2"
    }
  }
  
  it should "remove portals when onRemove" in {
    //given
    var ctx: PortalContext = null
    val ctxHook = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        ctx = useContext(Portal.Context)
        <.>()()
      }
    }
    val root = createTestRenderer(<(WithPortals())()(
      <(ctxHook()).empty,
      <.>()("some other content")
    )).root
    val portalId1 = ctx.onAdd(<.>()("portal content 1"))
    val portalId2 = ctx.onAdd(<.>()("portal content 2"))

    //when & then
    ctx.onRemove(portalId1)
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal2 shouldBe "portal content 2"
    }
    
    //when & then
    ctx.onRemove(portalId2)
    inside(root.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
    }
  }

  it should "return next portalId when getNextPortalId" in {
    //given
    val content = <.>()()
    var currId = getNextPortalId(Nil)
    
    //when & then
    getNextPortalId(List(currId -> content)) shouldBe (currId + 1)
    currId = currId + 1
    getNextPortalId(List((currId + 1) -> content)) shouldBe (currId + 2)
    currId = currId + 2
    getNextPortalId(List(currId -> content)) shouldBe (currId + 1)
    getNextPortalId(List(currId -> content)) shouldBe (currId + 2)
    getNextPortalId(List(currId -> content)) shouldBe (currId + 3)
  }
}
