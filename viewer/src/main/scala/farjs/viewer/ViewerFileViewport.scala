package farjs.viewer

import farjs.file.Encoding
import farjs.ui.UiString

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable

case class ViewerFileViewport(fileReader: ViewerFileReader,
                              encoding: String,
                              size: Double,
                              width: Int,
                              height: Int,
                              wrap: Boolean = false,
                              column: Int = 0,
                              position: Double = 0.0,
                              linesData: js.Array[ViewerFileLine] = new js.Array()) {
  
  lazy val content: String = {
    val buf = new mutable.StringBuilder()
    linesData.foldLeft(buf) { case (buf, line) =>
      buf.append(UiString(line.line).slice(column, column + width)).append('\n')
    }
    buf.mapInPlace { ch =>
      val isControl =
        ch == 0x00 ||   // nul
          ch == 0x07 || // bel
          ch == 0x08 || // backspace
          ch == 0x0b || // vertical tab
          ch == 0x1b || // ESC
          ch == 0x7f    // <DEL>

      if (isControl && ch != '\t' && ch != '\r' && ch != '\n') ' '
      else ch
    }
    buf.toString()
  }

  lazy val scrollIndicators: js.Array[Int] = {
    linesData.zipWithIndex.collect {
      case (ViewerFileLine(line, _), index) if UiString(line).strWidth() > column + width => index
    }
  }

  lazy val progress: Int = {
    val bytes = linesData.foldLeft(0)(_ + _.bytes)
    val viewed = position + bytes
    if (size == 0.0) 0
    else ((viewed / size) * 100).toInt
  }
  
  def moveUp(lines: Int, from: Double = position): js.Promise[ViewerFileViewport] = {
    if (from == 0.0) js.Promise.resolve[ViewerFileViewport](this)
    else {
      fileReader.readPrevLines(lines, from, size, encoding).toFuture
        .map(doWrap(lines, up = true))
        .map { data =>
          if (data.nonEmpty) {
            val bytes = data.map(_.bytes).sum
            copy(
              position = math.max(from - bytes, 0),
              linesData = 
                if (from == size) data
                else (data ++ linesData).take(height)
            )
          }
          else this
        }.toJSPromise
    }
  }

  def moveDown(lines: Int): js.Promise[ViewerFileViewport] = {
    val bytes = linesData.map(_.bytes).sum
    val nextPosition = position + bytes
    if (nextPosition >= size) {
      if (linesData.size == height) moveUp(height, from = size)
      else js.Promise.resolve[ViewerFileViewport](this)
    }
    else {
      fileReader.readNextLines(lines, nextPosition, encoding).toFuture
        .map(doWrap(lines, up = false))
        .map { data =>
          if (data.nonEmpty) {
            val bytes = linesData.take(lines).map(_.bytes).sum
            copy(
              position = position + bytes,
              linesData = linesData.drop(lines) ++ data
            )
          }
          else this
        }.toJSPromise
    }
  }

  def reload(from: Double = position): js.Promise[ViewerFileViewport] = {
    fileReader.readNextLines(height, from, encoding).toFuture
      .map(doWrap(height, up = false))
      .map { linesData =>
        copy(
          position = from,
          linesData = linesData
        )
      }.toJSPromise
  }

  private[viewer] def doWrap(lines: Int, up: Boolean)(data: js.Array[ViewerFileLine]): js.Array[ViewerFileLine] = {
    if (!wrap) data
    else {
      val res = new js.Array[ViewerFileLine]()

      def loop(inputLine: ViewerFileLine): Unit = {
        var fileLine = inputLine
        while (true) {
          val ViewerFileLine(line, bytes) = fileLine
          if (line.length <= width) {
            res.push(fileLine)
            return
          }

          val (wrapped, rest) =
            if (up) (line.takeRight(width), line.dropRight(width))
            else (line.take(width), line.drop(width))

          val wrappedBytes = Encoding.byteLength(wrapped, encoding)
          res.push(ViewerFileLine(wrapped, wrappedBytes))

          fileLine = ViewerFileLine(rest, math.max(bytes - wrappedBytes, 0))
        }
      }
      
      val resData =
        if (up) {
          data.foldRight(())((d, _) => loop(d))
          res.reverse.takeRight(lines)
        }
        else {
          data.foldLeft(())((_, d) => loop(d))
          res.take(lines)
        }

      resData
    }
  }
}
