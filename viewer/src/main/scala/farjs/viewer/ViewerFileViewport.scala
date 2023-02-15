package farjs.viewer

import farjs.ui.UI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ViewerFileViewport(fileReader: ViewerFileReader,
                              encoding: String,
                              size: Double,
                              width: Int,
                              height: Int,
                              position: Double = 0.0,
                              linesData: List[(String, Int)] = Nil) {
  
  lazy val content: String = {
    val buf = new StringBuilder()
    linesData.foldLeft(buf) { case (buf, (line, _)) =>
      buf.append(line.take(width)).append(UI.newLine)
    }
    buf.toString()
  }
  
  def moveUp(lines: Int, from: Double = position): Future[ViewerFileViewport] = {
    if (from == 0.0) Future.successful(this)
    else {
      fileReader.readPrevLines(lines, from, size, encoding).map { data =>
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
      fileReader.readNextLines(lines, nextPosition, encoding).map { data =>
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
    fileReader.readNextLines(height, from, encoding).map { linesData =>
      copy(
        position = from,
        linesData = linesData
      )
    }
  }
}
