package farjs.app.util

import scommons.nodejs.global
import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class LogControllerSpec extends TestSpec with TestRendererUtils {

  private val g: js.Dynamic = global.asInstanceOf[js.Dynamic]

  it should "render component and redirect log output" in {
    //given
    val oldLog = g.console.log
    val oldError = g.console.error
    val render = mockFunction[String, ReactElement]
    val props = LogControllerProps(render)
    val rendered: ReactElement = <.>()("some nested comp")

    render.expects("").returning(rendered)

    //when & then
    val renderer = createTestRenderer(<(LogController())(^.wrapped := props)())
    g.console.log should not be oldLog
    g.console.error should not be oldError
    
    renderer.root.children(0) shouldBe "some nested comp"
    
    //then
    render.expects("test message 1\ntest message 2\n").returning(rendered)

    //when & then
    TestRenderer.act { () =>
      println("test message 1")
      Console.err.println("test message 2")
    }
    renderer.root.children(0) shouldBe "some nested comp"
    
    //when & then
    TestRenderer.act { () =>
      renderer.unmount()
    }
    g.console.log shouldBe oldLog
    g.console.error shouldBe oldError
  }
}
