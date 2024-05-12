package farjs.file

import scommons.nodejs.raw.FSConstants
import scommons.nodejs.{Buffer, FS}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.control.NonFatal

class FileReader(fs: FS) {
  
  private[file] var fd: Int = 0

  def open(filePath: String): Future[Unit] = Future {
    fd = fs.openSync(filePath, FSConstants.O_RDONLY)
  }

  def close(): Future[Unit] = {
    Future(fs.closeSync(fd)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to close file, error: $ex")
    }
  }

  def readBytes(position: Double, buf: Buffer): Future[Int] = {
    fs.read(fd, buf, offset = 0, length = buf.length, position).andThen {
      case Failure(NonFatal(ex)) =>
        Console.err.println(s"Failed to read from file, error: $ex")
    }
  }
}
