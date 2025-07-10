package farjs.fs

import farjs.filelist.api.FileListItem
import farjs.fs.FSService.excludeRoots
import scommons.nodejs.Process.Platform
import scommons.nodejs.{path => nodePath, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FSService(platform: Platform, childProcess: ChildProcess) {

  def openItem(parent: String, item: String): Future[Unit] = {
    val name =
      if (item == FileListItem.up.name) FileListItem.currDir.name
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
        else s"""df -kP "$path""""
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
        else "df -kP"
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
          .filter(d => !excludeRoots.exists(d.root.startsWith))
          .map { d =>
            FSDisk.copy(d)(name = d.name.stripPrefix("/Volumes/"))
          }
      }
    }
  }
}

object FSService {

  lazy val instance: FSService = new FSService(process.platform, child_process)
  
  private lazy val excludeRoots = List(
    "/dev",
    "/net",
    "/home",
    "/private/",
    "/System/",
    "/etc/",
    "/sys/"
  )
}
