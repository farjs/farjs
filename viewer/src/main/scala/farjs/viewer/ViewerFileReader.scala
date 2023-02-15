package farjs.viewer

import farjs.viewer.ViewerFileReader.fs
import scommons.nodejs
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.{Buffer, FS}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Failure
import scala.util.control.NonFatal

object ViewerFileReader {

  private[viewer] var fs: FS = nodejs.fs
}

class ViewerFileReader(bufferSize: Int = 64 * 1024,
                       maxLineLength: Int = 1024) {
  
  private var fd: Int = 0

  def open(filePath: String): Future[Unit] = Future {
    fd = fs.openSync(filePath, FSConstants.O_RDONLY)
  }

  def close(): Future[Unit] = {
    Future(fs.closeSync(fd)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to close file, error: $ex")
    }
  }

  def readPrevLines(lines: Int, position: Double, maxPos: Double, encoding: String): Future[List[(String, Int)]] = {
    val res = new mutable.ArrayBuffer[(String, Int)](lines)
    var leftBuf: Buffer = null
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    @annotation.tailrec
    def loopOverBuffer(buf: Buffer, fromEnd: Boolean): Unit = {
      val suffix =
        if (buf.length > maxLineLength) buf.subarray(buf.length - maxLineLength, buf.length)
        else buf

      val rightNewLineIdx =
        if (fromEnd) suffix.length
        else suffix.lastIndexOf('\n'.toInt, suffix.length, encoding)
      val leftNewLineIdx =
        if (rightNewLineIdx <= 0) -1
        else suffix.lastIndexOf('\n'.toInt, rightNewLineIdx - 1, encoding)

      if (leftNewLineIdx < 0 && buf.length < maxLineLength) leftBuf = buf
      else {
        val (line, bytes) =
          if (rightNewLineIdx < 0) {
            val line = suffix.toString(encoding)
            val bytes = suffix.length
            (line, bytes)
          }
          else {
            if (leftNewLineIdx < 0) {
              val line = suffix.toString(encoding, start = 0, end = rightNewLineIdx)
              val bytes = suffix.length
              (line, bytes)
            }
            else {
              val line = suffix.toString(encoding, start = leftNewLineIdx + 1, end = rightNewLineIdx)
              val bytes = suffix.length - leftNewLineIdx - 1
              (line, bytes)
            }
          }
        res.prepend((line, bytes))

        if (res.length < lines && bytes < buf.length) {
          loopOverBuffer(buf.subarray(0, buf.length - bytes), fromEnd = false)
        }
      }
    }

    def loop(position: Double): Future[List[(String, Int)]] = {
      val (from, size) =
        if (position > bufSize) (position - bufSize, bufSize)
        else (0.0, position.toInt)
      
      readBytes(from, size).flatMap { buf =>
        loopOverBuffer(
          if (leftBuf != null) {
            val resBuf = Buffer.concat(js.Array(buf, leftBuf), buf.length + leftBuf.length)
            leftBuf = null
            resBuf
          }
          else buf,
          fromEnd = position == maxPos
        )
        
        if (res.length < lines && from > 0) loop(from)
        else {
          if (res.length < lines && leftBuf != null) {
            val line = leftBuf.toString(encoding, 0, leftBuf.length)
            val bytes = leftBuf.length
            res.prepend((line.trim, bytes))
          }
          Future.successful(res.toList)
        }
      }
    }

    if (position == 0.0) Future.successful(Nil)
    else logError(loop(position))
  }
  
  def readNextLines(lines: Int, position: Double, encoding: String): Future[List[(String, Int)]] = {
    val res = new mutable.ArrayBuffer[(String, Int)](lines)
    var leftBuf: Buffer = null
    val bufSize =
      if (lines > 1) bufferSize
      else maxLineLength

    @annotation.tailrec
    def loopOverBuffer(buf: Buffer): Unit = {
      val prefix = buf.subarray(0, maxLineLength)
      
      val newLineIndex = prefix.indexOf('\n'.toInt, 0, encoding)
      if (newLineIndex < 0 && buf.length < maxLineLength) leftBuf = buf
      else {
        val (line, bytes) =
          if (newLineIndex < 0) {
            val line = prefix.toString(encoding)
            val bytes = prefix.length
            (line, bytes)
          }
          else {
            val line = buf.toString(encoding, start = 0, end = newLineIndex)
            val bytes = newLineIndex + 1
            (line, bytes)
          }
        res.append((line, bytes))

        if (res.length < lines && bytes < buf.length) {
          loopOverBuffer(buf.subarray(bytes, buf.length))
        }
      }
    }

    def loop(position: Double): Future[List[(String, Int)]] = {
      readBytes(position, bufSize).flatMap { buf =>
        if (buf.length > 0) {
          loopOverBuffer(
            if (leftBuf != null) {
              val resBuf = Buffer.concat(js.Array(leftBuf, buf), leftBuf.length + buf.length)
              leftBuf = null
              resBuf
            }
            else buf
          )
        }
        
        if (res.length < lines && buf.length > 0) loop(position + buf.length)
        else {
          if (res.length < lines && leftBuf != null) {
            val line = leftBuf.toString(encoding, 0, leftBuf.length)
            val bytes = leftBuf.length
            res.append((line, bytes))
          }
          Future.successful(res.toList)
        }
      }
    }

    logError(loop(position))
  }
  
  private def readBytes(position: Double, size: Int): Future[Buffer] = {
    val buf = Buffer.allocUnsafe(size)
    fs.read(fd, buf, offset = 0, length = buf.length, position).map { bytesRead =>
      buf.subarray(0, bytesRead)
    }
  }
  
  private def logError(f: Future[List[(String, Int)]]): Future[List[(String, Int)]] = {
    f.andThen {
      case Failure(NonFatal(ex)) =>
        Console.err.println(s"Failed to read from file, error: $ex")
    }
  }
}
