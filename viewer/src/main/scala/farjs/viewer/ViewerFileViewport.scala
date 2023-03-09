package farjs.viewer

import farjs.text.Encoding

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ViewerFileViewport(fileReader: ViewerFileReader,
                              encoding: String,
                              size: Double,
                              width: Int,
                              height: Int,
                              wrap: Boolean = false,
                              column: Int = 0,
                              position: Double = 0.0,
                              linesData: List[(String, Int)] = Nil) {
  
  lazy val content: String = {
    val buf = new mutable.StringBuilder()
    linesData.foldLeft(buf) { case (buf, (line, _)) =>
      buf.append(line.slice(column, column + width)).append('\n')
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

  lazy val scrollIndicators: List[Int] = {
    linesData.zipWithIndex.collect {
      case ((line, _), index) if line.length > column + width => index
    }
  }

  lazy val progress: Int = {
    val bytes = linesData.foldLeft(0)(_ + _._2)
    val viewed = position + bytes
    if (size == 0.0) 0
    else ((viewed / size) * 100).toInt
  }
  
  def moveUp(lines: Int, from: Double = position): Future[ViewerFileViewport] = {
    if (from == 0.0) Future.successful(this)
    else {
      fileReader.readPrevLines(lines, from, size, encoding)
        .map(doWrap(lines, up = true))
        .map { data =>
          if (data.nonEmpty) {
            val bytes = data.map(_._2).sum
            copy(
              position = math.max(from - bytes, 0),
              linesData = 
                if (from == size) data
                else (data ++ linesData).take(height)
            )
          }
          else this
        }
    }
  }

  def moveDown(lines: Int): Future[ViewerFileViewport] = {
    val bytes = linesData.map(_._2).sum
    val nextPosition = position + bytes
    if (nextPosition >= size) {
      if (linesData.size == height) moveUp(height, from = size)
      else Future.successful(this)
    }
    else {
      fileReader.readNextLines(lines, nextPosition, encoding)
        .map(doWrap(lines, up = false))
        .map { data =>
          if (data.nonEmpty) {
            val bytes = linesData.take(lines).map(_._2).sum
            copy(
              position = position + bytes,
              linesData = linesData.drop(lines) ++ data
            )
          }
          else this
        }
    }
  }

  def reload(from: Double = position): Future[ViewerFileViewport] = {
    fileReader.readNextLines(height, from, encoding)
      .map(doWrap(height, up = false))
      .map { linesData =>
        copy(
          position = from,
          linesData = linesData
        )
      }
  }

  private def doWrap(lines: Int, up: Boolean)(data: List[(String, Int)]): List[(String, Int)] = {
    if (!wrap) data
    else {
      val res = new mutable.ArrayBuffer[(String, Int)](data.size)

      @annotation.tailrec
      def loop(data: (String, Int)): Unit = {
        val (line, bytes) = data
        if (line.length <= width) res.append(data)
        else {
          val prefix = line.take(width)
          val prefixBytes = Encoding.byteLength(prefix, encoding)
          res.append((prefix, prefixBytes))

          loop((line.drop(width), math.max(bytes - prefixBytes, 0)))
        }
      }
      
      data.foreach(loop)

      val resData =
        if (up) res.takeRight(lines)
        else res.take(lines)

      resData.toList
    }
  }
}
