package scommons.react.blessed.portal

import scommons.react._
import scommons.react.hooks._
import scommons.react.test.TestSpec
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

class WithPortalsSpec extends TestSpec
  with TestRendererUtils
  with ShallowRendererUtils {

  it should "add new portals when onRender" in {
    //given
    var ctx: WithPortalsContext = null
    val ctxHook = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        ctx = useContext(WithPortals.Context)
        <.>()()
      }
    }
    val root = createTestRenderer(<(WithPortals())()(
      <(ctxHook()).empty,
      <.>()("some other content")
    )).root

    //when & then
    ctx.onRender(1, <.>()("portal content 1"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "portal content 1"
    }
    
    //when & then
    ctx.onRender(2, <.>()("portal content 2"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "portal content 1"
      portal2 shouldBe "portal content 2"
    }
  }
  
  it should "update existing portals when onRender" in {
    //given
    var ctx: WithPortalsContext = null
    val ctxHook = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        ctx = useContext(WithPortals.Context)
        <.>()()
      }
    }
    val root = createTestRenderer(<(WithPortals())()(
      <(ctxHook()).empty,
      <.>()("some other content")
    )).root
    ctx.onRender(1, <.>()("portal content 1"))
    ctx.onRender(2, <.>()("portal content 2"))

    //when & then
    ctx.onRender(1, <.>()("updated content 1"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "updated content 1"
      portal2 shouldBe "portal content 2"
    }
    
    //when & then
    ctx.onRender(2, <.>()("updated content 2"))
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal1, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal1 shouldBe "updated content 1"
      portal2 shouldBe "updated content 2"
    }
  }
  
  it should "remove portals when onRemove" in {
    //given
    var ctx: WithPortalsContext = null
    val ctxHook = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        ctx = useContext(WithPortals.Context)
        <.>()()
      }
    }
    val root = createTestRenderer(<(WithPortals())()(
      <(ctxHook()).empty,
      <.>()("some other content")
    )).root
    ctx.onRender(1, <.>()("portal content 1"))
    ctx.onRender(2, <.>()("portal content 2"))

    //when & then
    ctx.onRemove(1)
    inside(root.children.toList) { case List(resCtxHook, otherContent, portal2) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
      portal2 shouldBe "portal content 2"
    }
    
    //when & then
    ctx.onRemove(2)
    inside(root.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe ctxHook()
      otherContent shouldBe "some other content"
    }
  }
  
  it should "set key when renderPortal" in {
    //given
    val id = 123
    val content = <.>()("some portal content")
    
    //when
    val result = WithPortals.renderPortal(id, content)
    
    //then
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        result
      }
    }
    assertNativeComponent(shallowRender(<(wrapper()).empty),
      <.>(
        ^.key := s"$id"
      )(content)
    )
  }
}
