package farjs.filelist.fs

import org.scalatest.Succeeded
import scommons.nodejs.ChildProcess._
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec
import scommons.nodejs.{ChildProcess, path => nodePath, _}

import scala.concurrent.Future
import scala.scalajs.js

class FSServiceSpec extends AsyncTestSpec {

  it should "open item in default app on Mac OS" in {
    //given
    val childProcess = mock[ChildProcess]
    val service = new FSService(Platform.darwin, childProcess)
    val parent = "test dir"
    val item = ".."
    val result = (new js.Object, new js.Object)

    //then
    (childProcess.exec _).expects(*, *).onCall { (command, options) =>
      command shouldBe """open ".""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item)

    //then
    resultF.map(_ => Succeeded)
  }

  it should "open item in default app on Windows" in {
    //given
    val childProcess = mock[ChildProcess]
    val service = new FSService(Platform.win32, childProcess)
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)

    //then
    (childProcess.exec _).expects(*, *).onCall { (command, options) =>
      command shouldBe s"""start "" "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item)

    //then
    resultF.map(_ => Succeeded)
  }

  it should "open item in default app on Linux" in {
    //given
    val childProcess = mock[ChildProcess]
    val service = new FSService(Platform.linux, childProcess)
    val parent = "test dir"
    val item = "file 1"
    val result = (new js.Object, new js.Object)

    //then
    (childProcess.exec _).expects(*, *).onCall { (command, options) =>
      command shouldBe s"""xdg-open "$item""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = parent
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.openItem(parent, item)

    //then
    resultF.map(_ => Succeeded)
  }

  it should "read disk info on Windows" in {
    //given
    val childProcess = mock[ChildProcess]
    val service = new FSService(Platform.win32, childProcess)
    val path = os.homedir()
    val output =
      """Caption  FreeSpace     Size          VolumeName
        |C:       81697124352   156595318784  SYSTEM
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    (childProcess.exec _).expects(*, *).onCall { (command, options) =>
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
    val resultF = service.readDisk(path)

    //then
    resultF.map { res =>
      res shouldBe Some(
        FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM")
      )
    }
  }

  it should "read disk info on Mac OS/Linux" in {
    //given
    val childProcess = mock[ChildProcess]
    val service = new FSService(Platform.darwin, childProcess)
    val path = os.homedir()
    val output =
      """Filesystem   1024-blocks      Used Available Capacity  Mounted on
        |/dev/disk1s1   244912536 202577024  40612004    84%    /
        |""".stripMargin
    val result: (js.Object, js.Object) = (output.asInstanceOf[js.Object], new js.Object)

    //then
    (childProcess.exec _).expects(*, *).onCall { (command, options) =>
      command shouldBe s"""df -kPl "$path""""
      assertObject(options.get, new ChildProcessOptions {
        override val cwd = path
        override val windowsHide = true
      })

      (null, Future.successful(result))
    }

    //when
    val resultF = service.readDisk(path)

    //then
    resultF.map { res =>
      res shouldBe Some(
        FSDisk("/", size = 250790436864.0, free = 41586692096.0, "/")
      )
    }
  }
}
