package farjs.ui

import org.scalactic.source.Position
import scommons.nodejs.test.TestSpec

class ListViewportSpec extends TestSpec {

  private val viewLength = 8
  
  it should "return None when onKeypress(unknown)" in {
    //given
    val viewport = ListViewport(0, 0, length = 5, viewLength)
    
    //when & then
    viewport.onKeypress("unknown") shouldBe None
  }

  it should "return same instance if length = 0 when onKeypress" in {
    //given
    val viewport = ListViewport(0, 0, length = 0, viewLength)
    
    def check(keyFull: String)(implicit pos: Position): Unit = {
      inside(viewport.onKeypress(keyFull)) {
        case Some(result) => result should be theSameInstanceAs viewport
      }
    }

    //when & then
    check("up")
    check("down")
    check("pageup")
    check("pagedown")
    check("home")
    check("end")
  }

  it should "return same instance if length = 1 when onKeypress" in {
    //given
    val viewport = ListViewport(0, 0, length = 1, viewLength)
    
    def check(keyFull: String)(implicit pos: Position): Unit = {
      inside(viewport.onKeypress(keyFull)) {
        case Some(result) => result should be theSameInstanceAs viewport
      }
    }

    //when & then
    check("up")
    check("down")
    check("pageup")
    check("pagedown")
    check("home")
    check("end")
  }

  it should "return updated instance if length < viewLength when onKeypress" in {
    //given
    var viewport = ListViewport(0, 0, length = 5, viewLength)
    viewport.length should be < viewport.viewLength
    
    def check(keyFull: String, focused: Int)(implicit pos: Position): Unit = {
      inside(viewport.onKeypress(keyFull)) {
        case Some(result) =>
          result shouldBe viewport.copy(focused = focused)
          viewport = result
      }
    }

    //when & then
    check("up", 0)
    check("pageup", 0)
    check("home", 0)
    check("down", 1)
    check("down", 2)
    check("pagedown", 4)
    check("pageup", 0)
    check("end", 4)
    check("down", 4)
    check("pagedown", 4)
    check("up", 3)
    check("up", 2)
    check("home", 0)
  }

  it should "return updated instance if length > viewLength when onKeypress" in {
    //given
    var viewport = ListViewport(0, 0, length = 10, viewLength)
    viewport.length should be > viewport.viewLength
    
    def check(keyFull: String, offset: Int, focused: Int)(implicit pos: Position): Unit = {
      inside(viewport.onKeypress(keyFull)) {
        case Some(result) =>
          result shouldBe viewport.copy(offset = offset, focused = focused)
          viewport = result
      }
    }

    //when & then
    check("up", 0, 0)
    check("pageup", 0, 0)
    check("home", 0, 0)
    check("down", 0, 1)
    check("down", 0, 2)
    check("pagedown", 2, 2)
    check("pagedown", 2, 7)
    check("pageup", 0, 7)
    check("pageup", 0, 0)
    check("down", 0, 1)
    check("down", 0, 2)
    check("down", 0, 3)
    check("down", 0, 4)
    check("down", 0, 5)
    check("down", 0, 6)
    check("down", 0, 7)
    check("down", 1, 7)
    check("down", 2, 7)
    check("down", 2, 7)
    check("end", 2, 7)
    check("pagedown", 2, 7)
    check("up", 2, 6)
    check("up", 2, 5)
    check("up", 2, 4)
    check("up", 2, 3)
    check("up", 2, 2)
    check("up", 2, 1)
    check("up", 2, 0)
    check("up", 1, 0)
    check("up", 0, 0)
    check("up", 0, 0)
    check("end", 2, 7)
    check("home", 0, 0)
  }
}
