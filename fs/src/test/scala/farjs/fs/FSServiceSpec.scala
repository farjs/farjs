package farjs.fs

import farjs.filelist.MockChildProcess
import farjs.fs.FSDiskSpec.{assertFSDisk, assertFSDisks}
import org.scalatest.{OptionValues, Succeeded}
import scommons.nodejs.ChildProcess._
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.{path => nodePath, _}

import scala.concurrent.Future
import scala.scalajs.js

class FSServiceSpec extends AsyncTestSpec with OptionValues {

  //noinspection TypeAnnotation
  class ChildProcess {
    val exec = mockFunction[String, Option[ChildProcessOptions],
      (raw.ChildProcess, Future[(js.Object, js.Object)])]
    
    val childProcess = new MockChildProcess(
      execMock = exec
    )
  }
  
  it should "open item in default app on Mac OS" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.darwin, childProcess.childProcess)
    val parent = "test dir"
    val item = ".."
    val result = (new js.Object, new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe """open ".""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item).toFuture

    //then
    resultF.map(_ => Succeeded)
  }

  it should "open item in default app on Windows" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.win32, childProcess.childProcess)
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe s"""start "" "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item).toFuture

    //then
    resultF.map(_ => Succeeded)
  }

  it should "open item in default app on Linux" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.linux, childProcess.childProcess)
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe s"""xdg-open "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item).toFuture

    //then
    resultF.map(_ => Succeeded)
  }

  it should "read disk info on Windows" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.win32, childProcess.childProcess)
    val path = os.homedir()
    val output =
      """Caption  FreeSpace     Size          VolumeName
        |C:       81697124352   156595318784  SYSTEM
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      val root = nodePath.parse(path).root.map(_.stripSuffix("\\"))
      command shouldBe {
        s"""wmic logicaldisk where "Caption='$root'" get Caption,VolumeName,FreeSpace,Size"""
      }
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = path
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.readDisk(path).toFuture

    //then
    resultF.map { res =>
      assertFSDisk(res.toOption.value,
        FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM")
      )
    }
  }

  it should "read disk info on Mac OS/Linux" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.darwin, childProcess.childProcess)
    val path = os.homedir()
    val output =
      """Filesystem   1024-blocks      Used Available Capacity  Mounted on
        |/dev/disk1s1   244912536 202577024  40612004    84%    /
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe s"""df -kP "$path""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = path
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.readDisk(path).toFuture

    //then
    resultF.map { res =>
      assertFSDisk(res.toOption.value,
        FSDisk("/", size = 250790436864.0, free = 41586692096.0, "/")
      )
    }
  }

  it should "read disks on Windows" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.win32, childProcess.childProcess)
    val output =
      """Caption  FreeSpace     Size          VolumeName
        |C:       81697124352   156595318784  SYSTEM
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe "wmic logicaldisk get Caption,VolumeName,FreeSpace,Size"
      assertObject(options.get, new ChildProcessOptions {
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.readDisks().toFuture

    //then
    resultF.map { res =>
      assertFSDisks(res.toList, List(
        FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM")
      ))
    }
  }

  it should "read disks on Mac OS/Linux" in {
    //given
    val childProcess = new ChildProcess
    val service = new FSService(Platform.darwin, childProcess.childProcess)
    val output =
      """Filesystem   1024-blocks      Used Available Capacity  Mounted on
        |/dev/disk1s1   244912536 202577024  40612004    84%    /
        |devfs                234       234         0   100%    /dev
        |/dev/disk1s3   244912536   4194424  59259185     8%    /System/Volumes/VM
        |/dev/disk1s4   244912536   4194424  59259180     7%    /private/var/vm
        |map -hosts             0         0         0   100%    /net
        |map auto_home          0         0         0   100%    /home
        |/dev/disk2s1     1957408     14752   1942656     1%    /Volumes/FLASHDRIVE
        |/dev/vda1        1957408     14752   1942656    45%    /etc/hosts
        |tmpfs            1018208         0   1018208     0%    /sys/firmware
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    childProcess.exec.expects(*, *).onCall { (command, options) =>
      command shouldBe "df -kP"
      assertObject(options.get, new ChildProcessOptions {
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.readDisks().toFuture

    //then
    resultF.map { res =>
      assertFSDisks(res.toList, List(
        FSDisk("/", size = 250790436864.0, free = 41586692096.0, "/"),
        FSDisk("/Volumes/FLASHDRIVE", size = 2004385792.0, free = 1989279744.0, "FLASHDRIVE")
      ))
    }
  }
}
