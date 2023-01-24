package farjs.viewer

import farjs.viewer.ViewerFileReader.fs
import scommons.nodejs
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.{Buffer, FS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.control.NonFatal

object ViewerFileReader {

  private[viewer] var fs: FS = nodejs.fs
}

class ViewerFileReader {
  
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

  def readPageAt(position: Double, encoding: String): Future[(String, Int)] = {
    val buff = Buffer.allocUnsafe(64 * 1024)
    fs.read(fd, buff, offset = 0, length = buff.length, position).map { bytesRead =>
      val page = buff.toString(encoding, start = 0, end = bytesRead)
      (page, bytesRead)
    }.andThen {
      case Failure(NonFatal(ex)) =>
        Console.err.println(s"Failed to read from file, error: $ex")
    }
  }
}
