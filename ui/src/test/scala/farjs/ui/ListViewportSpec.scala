package farjs.ui

import org.scalactic.source.Position
import scommons.nodejs.test.TestSpec

class ListViewportSpec extends TestSpec {

  private val viewLength = 8
  
  it should "return None when onKeypress(unknown)" in {
    //given
    val viewport = ListViewport(0, length = 5, viewLength)
    
    //when & then
    viewport.onKeypress("unknown") shouldBe None
  }

  it should "return same instance if length = 0 when onKeypress" in {
    //given
    val viewport = ListViewport(0, length = 0, viewLength)
    
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
    val viewport = ListViewport(0, length = 1, viewLength)
    
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
    var viewport = ListViewport(0, length = 5, viewLength)
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
    var viewport = ListViewport(0, length = 10, viewLength)
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

  it should "return same instance if newViewLength = viewLength when resize" in {
    //given
    val viewport = ListViewport(0, length = 0, viewLength)

    //when & then
    viewport.resize(viewport.viewLength) should be theSameInstanceAs viewport
  }

  it should "return updated instance when resize" in {
    //given
    var viewport = ListViewport(9, length = 10, 8)

    def check(offset: Int, focused: Int, newViewLength: Int)(implicit pos: Position): Unit = {
      val result = viewport.resize(newViewLength)
      result shouldBe viewport.copy(
        offset = offset,
        focused = focused,
        viewLength = newViewLength
      )
      viewport = result
    }

    //when & then
    check(offset = 1, focused = 8, newViewLength = 9)
    check(offset = 0, focused = 9, newViewLength = 10)
    check(offset = 0, focused = 9, newViewLength = 11)
    check(offset = 0, focused = 9, newViewLength = 10)
    check(offset = 1, focused = 8, newViewLength = 9)
    check(offset = 2, focused = 7, newViewLength = 8)
    check(offset = 3, focused = 6, newViewLength = 7)
    check(offset = 4, focused = 5, newViewLength = 6)
    check(offset = 5, focused = 4, newViewLength = 5)
    check(offset = 6, focused = 3, newViewLength = 4)
    check(offset = 7, focused = 2, newViewLength = 3)
    check(offset = 8, focused = 1, newViewLength = 2)
    check(offset = 9, focused = 0, newViewLength = 1)
    check(offset = 8, focused = 1, newViewLength = 2)
    check(offset = 7, focused = 2, newViewLength = 3)
  }

  it should "return new instance with normalized offset and focused when apply" in {

    def check(result: ListViewport, offset: Int, focused: Int)(implicit pos: Position): Unit = {
      inside(result) { case ListViewport(resOffset, resFocused, _, _) =>
        resOffset shouldBe offset
        resFocused shouldBe focused
      }
    }

    //when & then
    check(ListViewport(9, length = 10, 0), offset = 0, focused = 9)
    check(ListViewport(0, length = 10, 8), offset = 0, focused = 0)
    check(ListViewport(1, length = 10, 8), offset = 0, focused = 1)
    check(ListViewport(2, length = 10, 8), offset = 0, focused = 2)
    check(ListViewport(7, length = 10, 8), offset = 0, focused = 7)
    check(ListViewport(8, length = 10, 8), offset = 2, focused = 6)
    check(ListViewport(9, length = 10, 8), offset = 2, focused = 7)
    check(ListViewport(0, length = 20, 8), offset = 0, focused = 0)
    check(ListViewport(1, length = 20, 8), offset = 0, focused = 1)
    check(ListViewport(7, length = 20, 8), offset = 0, focused = 7)
    check(ListViewport(8, length = 20, 8), offset = 8, focused = 0)
    check(ListViewport(9, length = 20, 8), offset = 8, focused = 1)
    check(ListViewport(10, length = 20, 8), offset = 8, focused = 2)
    check(ListViewport(11, length = 20, 8), offset = 8, focused = 3)
    check(ListViewport(15, length = 20, 8), offset = 8, focused = 7)
    check(ListViewport(16, length = 20, 8), offset = 12, focused = 4)
    check(ListViewport(17, length = 20, 8), offset = 12, focused = 5)
    check(ListViewport(19, length = 20, 8), offset = 12, focused = 7)
  }
}
