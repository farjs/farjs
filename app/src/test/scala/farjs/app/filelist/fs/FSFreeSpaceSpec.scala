package farjs.app.filelist.fs

import farjs.filelist.FileListActions.FileListDiskSpaceUpdatedAction
import farjs.filelist.api.FileListDir
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

import scala.concurrent.{Future, Promise}

class FSFreeSpaceSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  //noinspection TypeAnnotation
  class FsService {
    val readDisk = mockFunction[String, Future[Option[FSDisk]]]

    val fsService = new MockFSService(
      readDiskMock = readDisk
    )
  }

  it should "dispatch action when readDisk returns Some(...)" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSFreeSpace.fsService = fsService.fsService
    val props = FSFreeSpaceProps(dispatch, FileListDir("/", isRoot = false, Nil))
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val resultF = Future.successful(Some(disk))
    val action = FileListDiskSpaceUpdatedAction(disk.free)

    //then
    fsService.readDisk.expects(props.currDir.path).returning(resultF)
    dispatch.expects(action)
    
    //when
    val result = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)()).root

    //then
    result.children.toList should be (empty)
    resultF.map(_ => Succeeded)
  }

  it should "not dispatch action when readDisk returns None" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSFreeSpace.fsService = fsService.fsService
    val props = FSFreeSpaceProps(dispatch, FileListDir("/", isRoot = false, Nil))
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val resultF = Future.successful(Some(disk))
    val action = FileListDiskSpaceUpdatedAction(disk.free)

    fsService.readDisk.expects(props.currDir.path).returning(resultF)
    dispatch.expects(action)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val resultF2 = Future.successful(Option.empty[FSDisk])

    resultF.flatMap { _ =>
      //then
      fsService.readDisk.expects(props2.currDir.path).returning(resultF2)
      dispatch.expects(*).never()

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      renderer.root.children.toList should be (empty)
      resultF2.map(_ => Succeeded)
    }
  }

  it should "not dispatch action when readDisk fails" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSFreeSpace.fsService = fsService.fsService
    val props = FSFreeSpaceProps(dispatch, FileListDir("/", isRoot = false, Nil))
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val resultF = Future.successful(Some(disk))
    val action = FileListDiskSpaceUpdatedAction(disk.free)

    fsService.readDisk.expects(props.currDir.path).returning(resultF)
    dispatch.expects(action)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val resultF2 = Future.failed(new Exception("test error"))

    resultF.flatMap { _ =>
      //then
      fsService.readDisk.expects(props2.currDir.path).returning(resultF2)
      dispatch.expects(*).never()

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      renderer.root.children.toList should be (empty)
      resultF2.failed.map(_ => Succeeded)
    }
  }

  it should "not call readDisk if currDir is not changed when re-render" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSFreeSpace.fsService = fsService.fsService
    val props = FSFreeSpaceProps(dispatch, FileListDir("/", isRoot = false, Nil))
    val disk = FSDisk("/", size = 123.0, free = 456.0, "/")
    val resultF = Future.successful(Some(disk))
    val action = FileListDiskSpaceUpdatedAction(disk.free)

    //then
    fsService.readDisk.expects(props.currDir.path).returning(resultF)
    dispatch.expects(action)

    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    val props2 = props.copy(currDir = props.currDir)

    resultF.map { _ =>
      //then
      fsService.readDisk.expects(*).never()
      dispatch.expects(*).never()

      //when
      TestRenderer.act { () =>
        renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
      }

      //then
      renderer.root.children.toList should be (empty)
    }
  }

  it should "dispatch action only for the same (current) dir instance" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val fsService = new FsService
    FSFreeSpace.fsService = fsService.fsService
    val props = FSFreeSpaceProps(dispatch, FileListDir("/", isRoot = false, Nil))
    val props2 = props.copy(currDir = props.currDir.copy(path = "/2"))
    val disk1 = FSDisk("/", size = 123.0, free = 456.0, "/")
    val disk2 = FSDisk("/2", size = 124.0, free = 457.0, "/")
    val p1 = Promise[Option[FSDisk]]()
    val resultF1 = p1.future
    val p2 = Promise[Option[FSDisk]]()
    val resultF2 = p2.future
    val action = FileListDiskSpaceUpdatedAction(disk2.free)

    //then
    fsService.readDisk.expects(props.currDir.path).returning(resultF1)
    fsService.readDisk.expects(props2.currDir.path).returning(resultF2)
    dispatch.expects(action)

    //when
    val renderer = createTestRenderer(<(FSFreeSpace())(^.wrapped := props)())
    TestRenderer.act { () =>
      renderer.update(<(FSFreeSpace())(^.wrapped := props2)())
    }
    
    //then
    renderer.root.children.toList should be (empty)
    p2.success(Some(disk2))
    p1.success(Some(disk1))
    resultF1.flatMap { _ =>
      resultF2.map(_ => Succeeded)
    }
  }
}
