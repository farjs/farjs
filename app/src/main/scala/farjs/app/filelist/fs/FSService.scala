package farjs.app.filelist.fs

import farjs.filelist.api.{FileListDir, FileListItem}
import scommons.nodejs.Process.Platform
import scommons.nodejs.{path => nodePath, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FSService(platform: Platform, childProcess: ChildProcess) {

  def openItem(parent: String, item: String): Future[Unit] = {
    val name =
      if (item == FileListItem.up.name) FileListDir.curr
      else item

    val (_, future) = childProcess.exec(
      command = {
        if (platform == Platform.darwin) s"""open "$name""""
        else if (platform == Platform.win32) s"""start "" "$name""""
        else s"""xdg-open "$name""""
      },
      options = Some(new raw.ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })
    )

    future.map(_ => ())
  }

  def readDisk(path: String): Future[Option[FSDisk]] = {
    val (_, future) = childProcess.exec(
      command = {
        if (platform == Platform.win32) {
          val root = nodePath.parse(path).root.map(_.stripSuffix("\\"))
          s"""wmic logicaldisk where "Caption='$root'" get Caption,VolumeName,FreeSpace,Size"""
        }
        else s"""df -kPl "$path""""
      },
      options = Some(new raw.ChildProcessOptions {
        override val cwd = path
        override val windowsHide = true
      })
    )

    future.map { case (stdout, _) =>
      val output = stdout.asInstanceOf[String]
      val disks =
        if (platform == Platform.win32) FSDisk.fromWmicLogicalDisk(output)
        else FSDisk.fromDfCommand(output)

      disks.headOption
    }
  }

  def readDisks(): Future[List[FSDisk]] = {
    val (_, future) = childProcess.exec(
      command = {
        if (platform == Platform.win32) {
          "wmic logicaldisk get Caption,VolumeName,FreeSpace,Size"
        }
        else "df -kPl"
      },
      options = Some(new raw.ChildProcessOptions {
        override val windowsHide = true
      })
    )

    future.map { case (stdout, _) =>
      val output = stdout.asInstanceOf[String]
      if (platform == Platform.win32) FSDisk.fromWmicLogicalDisk(output)
      else {
        FSDisk.fromDfCommand(output)
          .filter(d => !d.root.startsWith("/private/") && !d.root.startsWith("/System/"))
          .map { d =>
            d.copy(name = d.name.stripPrefix("/Volumes/"))
          }
      }
    }
  }
}

object FSService {

  lazy val instance: FSService = new FSService(process.platform, child_process)
}
