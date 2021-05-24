package farjs.filelist.fs

import org.scalatest.Succeeded
import scommons.nodejs.ChildProcess
import scommons.nodejs.ChildProcess._
import scommons.nodejs.Process.Platform
import scommons.nodejs.test.AsyncTestSpec

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
}
