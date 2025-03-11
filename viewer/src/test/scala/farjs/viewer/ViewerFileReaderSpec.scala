package farjs.viewer

import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{Assertion, Succeeded}

import scala.scalajs.js

object ViewerFileReaderSpec {
  
  def assertViewerFileLines(results: js.Array[ViewerFileLine], lines: List[ViewerFileLine]): Assertion = {
    results.length shouldBe lines.size
    results.toList.zip(lines).foreach { case (result, expected) =>
      assertViewerFileLine(result, expected)
    }
    Succeeded
  }
  
  def assertViewerFileLine(result: ViewerFileLine, expected: ViewerFileLine): Assertion = {
    inside(result) {
      case ViewerFileLine(line, bytes) =>
        line shouldBe expected.line
        bytes shouldBe expected.bytes
    }
  }
}
