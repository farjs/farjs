package farjs.filelist.fs

import farjs.filelist.api.FileListDir
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class FSFreeSpaceSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  it should "call onRender(Some(...)) when readDisk returns Some(...)" in {
    //given
    val onRender = mockFunction[Option[Double], ReactElement]
    val fsService = mock[FSService]
    FSFreeSpace.fsService = fsService
    val props = FSFreeSpaceProps(FileListDir("/", isRoot = false, Nil), onRender)
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val renderRes = <.button()()
    val resultF = Future.successful(Some(disk))

    //then
    (fsService.readDisk _).expects(props.currDir.path).returning(resultF)
    onRender.expects(None).returning(renderRes)
    onRender.expects(Some(disk.free)).returning(renderRes)
    
    //when
    val result = testRender(<(FSFreeSpace())(^.wrapped := props)())

    //then
    assertNativeComponent(result, renderRes)
    resultF.map(_ => Succeeded)
  }

  it should "call onRender(None) when readDisk returns None" in {
    //given
    val onRender = mockFunction[Option[Double], ReactElement]
    val fsService = mock[FSService]
    FSFreeSpace.fsService = fsService
    val props = FSFreeSpaceProps(FileListDir("/", isRoot = false, Nil), onRender)
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val renderRes = <.button()()
    val resultF = Future.successful(Some(disk))

    (fsService.readDisk _).expects(props.currDir.path).returning(resultF)
    onRender.expects(None).returning(renderRes)
    onRender.expects(Some(disk.free)).returning(renderRes)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val resultF2 = Future.successful(Option.empty[FSDisk])

    resultF.flatMap { _ =>
      //then
      (fsService.readDisk _).expects(props2.currDir.path).returning(resultF2)
      onRender.expects(Some(disk.free)).returning(renderRes)
      onRender.expects(None).returning(renderRes)

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      assertNativeComponent(renderer.root.children(0), renderRes)
      resultF2.map(_ => Succeeded)
    }
  }

  it should "call onRender(None) when readDisk fails" in {
    //given
    val onRender = mockFunction[Option[Double], ReactElement]
    val fsService = mock[FSService]
    FSFreeSpace.fsService = fsService
    val props = FSFreeSpaceProps(FileListDir("/", isRoot = false, Nil), onRender)
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val renderRes = <.button()()
    val resultF = Future.successful(Some(disk))

    (fsService.readDisk _).expects(props.currDir.path).returning(resultF)
    onRender.expects(None).returning(renderRes)
    onRender.expects(Some(disk.free)).returning(renderRes)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val resultF2 = Future.failed(new Exception("test error"))

    resultF.flatMap { _ =>
      //then
      (fsService.readDisk _).expects(props2.currDir.path).returning(resultF2)
      onRender.expects(Some(disk.free)).returning(renderRes)
      onRender.expects(None).returning(renderRes)

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      assertNativeComponent(renderer.root.children(0), renderRes)
      resultF2.failed.map(_ => Succeeded)
    }
  }

  it should "not call readDisk if currDir is not changed when re-render" in {
    //given
    val onRender = mockFunction[Option[Double], ReactElement]
    val fsService = mock[FSService]
    FSFreeSpace.fsService = fsService
    val props = FSFreeSpaceProps(FileListDir("/", isRoot = false, Nil), onRender)
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val renderRes = <.button()()
    val resultF = Future.successful(Some(disk))

    (fsService.readDisk _).expects(props.currDir.path).returning(resultF)
    onRender.expects(None).returning(renderRes)
    onRender.expects(Some(disk.free)).returning(renderRes)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir)

    resultF.map { _ =>
      //then
      onRender.expects(Some(disk.free)).returning(renderRes)

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      assertNativeComponent(renderer.root.children(0), renderRes)
    }
  }

  it should "update state and call onRender only for the same (current) dir instance" in {
    //given
    val onRender = mockFunction[Option[Double], ReactElement]
    val fsService = mock[FSService]
    FSFreeSpace.fsService = fsService
    val props = FSFreeSpaceProps(FileListDir("/", isRoot = false, Nil), onRender)
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val disk1 = FSDisk("/", size = 123.0, free = 456.0, "/")
    val disk2 = FSDisk("/2", size = 124.0, free = 457.0, "/")
    val renderRes = <.button()()
    val p1 = Promise[Option[FSDisk]]()
    val resultF1 = p1.future
    val p2 = Promise[Option[FSDisk]]()
    val resultF2 = p2.future

    //then
    (fsService.readDisk _).expects(props.currDir.path).returning(resultF1)
    (fsService.readDisk _).expects(props2.currDir.path).returning(resultF2)
    onRender.expects(None).returning(renderRes)
    onRender.expects(None).returning(renderRes)
    onRender.expects(Some(disk2.free)).returning(renderRes)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())

    //when
    TestRenderer.act { () =>
      renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
    }
    
    //then
    p2.success(Some(disk2))
    p1.success(Some(disk1))
    resultF1.flatMap { _ =>
      resultF2.map(_ => Succeeded)
    }
  }
}
