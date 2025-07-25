package farjs.fs

import farjs.filelist.api.FileListItemSpec.{assertFileListItem, assertFileListItems}
import farjs.filelist.api._
import org.scalatest.Succeeded
import scommons.nodejs.Process.Platform
import scommons.nodejs._
import scommons.nodejs.raw.{FSConstants, FileOptions}
import scommons.nodejs.test.AsyncTestSpec

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

class FSFileListApiSpec extends AsyncTestSpec {
  
  private val apiImp = new FSFileListApi

  //noinspection TypeAnnotation
  class FsService {
    val readDisk = mockFunction[String, js.Promise[js.UndefOr[FSDisk]]]

    val fsService = new MockFSService(
      readDiskMock = readDisk
    )
  }

  it should "return supported capabilities" in {
    //when & then
    apiImp.capabilities.toSet shouldBe Set(
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete,
      FileListCapability.mkDirs,
      FileListCapability.copyInplace,
      FileListCapability.moveInplace
    )
  }

  it should "not fail if fs.lstatSync fail when readDir" in {
    //given
    val readdirMock = mockFunction[String, Future[Seq[String]]]
    val lstatSyncMock = mockFunction[String, Stats]
    val fs = new FS {
      override def readdir(path: String): Future[Seq[String]] = readdirMock(path)
      override def lstatSync(path: String): Stats = lstatSyncMock(path)
    }
    val apiImp = new FSFileListApi(fs)
    val targetDir = path.resolve(FileListItem.currDir.name)

    readdirMock.expects(targetDir).returning(Future.successful(List("file1", "file2")))
    lstatSyncMock.expects(path.join(targetDir, "file1")).throwing(new Exception("test error"))
    lstatSyncMock.expects(path.join(targetDir, "file2")).throwing(new Exception("test error"))
    
    //when
    apiImp.readDir(targetDir, js.undefined).toFuture.map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe process.cwd()
        isRoot shouldBe false
        assertFileListItems(items.toList, List(
          FileListItem("file1"),
          FileListItem("file2")
        ))
      }
    }
  }
  
  it should "return current dir info and files when readDir('', .)" in {
    //when
    apiImp.readDir("", FileListItem.currDir.name).toFuture.map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe process.cwd()
        isRoot shouldBe false
        items.toList should not be empty
      }
    }
  }
  
  it should "return parent dir info and files when readDir(dir, ..)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val parentDir = currDirObj.dir.getOrElse("")
    val currDir = currDirObj.base.getOrElse("")
    parentDir should not be empty
    currDir should not be empty

    //when
    apiImp.readDir(curr, FileListItem.up.name).toFuture.map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe parentDir
        isRoot shouldBe false
        items.toList should not be empty
      }
    }
  }
  
  it should "return target dir info and files when readDir(dir, sub-dir)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val parentDir = currDirObj.dir.getOrElse("")
    val subDir = currDirObj.base.getOrElse("")
    parentDir should not be empty
    subDir should not be empty

    //when
    apiImp.readDir(parentDir, subDir).toFuture.map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe curr
        isRoot shouldBe false
        items.toList should not be empty
      }
    }
  }
  
  it should "return root dir info and files when readDir(root, .)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val currRoot = currDirObj.root.getOrElse("")
    currRoot should not be empty

    //when
    apiImp.readDir(currRoot, FileListItem.currDir.name).toFuture.map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe currRoot
        isRoot shouldBe true
        items.toList should not be empty
      }
    }
  }
  
  it should "delete items when delete" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true

    def create(parent: String, name: String, isDir: Boolean): (String, String) = {
      val fullPath = path.join(parent, name)
      if (isDir) fs.mkdirSync(fullPath)
      else fs.writeFileSync(fullPath, s"file: $fullPath")
      (fullPath, name)
    }
    
    val (d1, d1Name) = create(tmpDir, "dir1", isDir = true)
    val (f1, f1Name) = create(tmpDir, "file1.txt", isDir = false)
    val (d2, _) = create(d1, "dir2", isDir = true)
    val (f2, _) = create(d1, "file2.txt", isDir = false)
    val (d3, _) = create(d2, "dir3", isDir = true)
    val (f3, _) = create(d2, "file3.txt", isDir = false)
    val items = js.Array(
      FileListItem(d1Name, isDir = true),
      FileListItem(f1Name)
    )

    //when
    val resultF = apiImp.delete(tmpDir, items).toFuture

    //then
    val resCheckF = resultF.map { _ =>
      fs.existsSync(d1) shouldBe false
      fs.existsSync(f1) shouldBe false
      fs.existsSync(d2) shouldBe false
      fs.existsSync(f2) shouldBe false
      fs.existsSync(d3) shouldBe false
      fs.existsSync(f3) shouldBe false
    }
    
    //cleanup
    resCheckF.map { _ =>
      del(f3, isDir = false)
      del(f2, isDir = false)
      del(f1, isDir = false)
      del(d3, isDir = true)
      del(d2, isDir = true)
      del(d1, isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "create multiple directories when mkDirs" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val dirs = List("test1", "test2", "", "test3", "")
    val resPath = path.join(tmpDir :: dirs: _*)

    //when
    val resultF = apiImp.mkDirs(js.Array(tmpDir :: dirs: _*)).toFuture

    //then
    val resCheckF = resultF.map { res =>
      res shouldBe resPath
      fs.existsSync(resPath) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(resPath, isDir = true)
      del(path.join(tmpDir, "test1", "test2"), isDir = true)
      del(path.join(tmpDir, "test1"), isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "not fail if dirs already exists when mkDirs" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val topDir = "test1"
    val dirs = List(topDir, "test2", "", "test3", "")
    val resPath = path.join(tmpDir :: dirs: _*)
    fs.mkdirSync(path.join(tmpDir, topDir))
    fs.existsSync(path.join(tmpDir, topDir)) shouldBe true

    //when
    val resultF = apiImp.mkDirs(js.Array(tmpDir :: dirs: _*)).toFuture

    //then
    val resCheckF = resultF.map { res =>
      res shouldBe resPath
      fs.existsSync(resPath) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(resPath, isDir = true)
      del(path.join(tmpDir, "test1", "test2"), isDir = true)
      del(path.join(tmpDir, "test1"), isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "create single directory when mkDirs" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val dir = "test123"
    val resPath = path.join(tmpDir, dir)

    //when
    val resultF = apiImp.mkDirs(js.Array(resPath)).toFuture

    //then
    val resCheckF = resultF.map { res =>
      res shouldBe resPath
      fs.existsSync(resPath) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(resPath, isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "skip root directory creation when mkDirs" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val tmpDirObj = path.parse(tmpDir)
    val tmpRoot = tmpDirObj.root.getOrElse("")
    tmpRoot should not be empty
    val tmpRest = tmpDir.stripPrefix(tmpRoot).stripPrefix(path.sep)
    val dir = "skip_root_dir_creation_test"
    val resPath = path.join(tmpRoot, tmpRest, dir)
    fs.existsSync(resPath) shouldBe false

    //when
    val resultF = apiImp.mkDirs(js.Array(tmpRoot, tmpRest, dir)).toFuture

    //then
    val resCheckF = resultF.map { res =>
      res shouldBe resPath
      fs.existsSync(resPath) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(resPath, isDir = true)
      del(tmpDir, isDir = true)
      fs.existsSync(resPath) shouldBe false
    }
  }
  
  it should "copy new file when readFile/writeFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true

    val file1 = path.join(tmpDir, "example.txt")
    val file2 = path.join(tmpDir, "example2.txt")
    fs.writeFileSync(file1, "hello, World!!!")
    fs.existsSync(file1) shouldBe true

    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val stats1 = fs.lstatSync(file1)
    val buff = new Uint8Array(5)

    def loop(source: FileSource, target: FileTarget): Future[Unit] = {
      source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
        if (bytesRead == 0) target.setAttributes(getFileListItem("example.txt", stats1)).toFuture
        else target.writeNextBytes(buff, bytesRead).toFuture.flatMap(_ => loop(source, target))
      }
    }
    
    //then
    onExists.expects(*).never()
    
    //when
    val resultF = for {
      source <- apiImp.readFile(tmpDir, FileListItem("example.txt"), 0.0).toFuture
      _ = source.file shouldBe file1
      maybeTarget <- apiImp.writeFile(tmpDir, "example2.txt", onExists).toFuture
      _ <- maybeTarget.map(loop(source, _)).getOrElse(Future.unit)
      _ <- maybeTarget.map(_.close().toFuture).getOrElse(Future.unit)
      _ <- source.close().toFuture
    } yield ()

    resultF.map { _ =>
      //then
      val stats2 = fs.lstatSync(file2)
      stats2.size shouldBe stats1.size
      toDateTimeStr(stats2.atimeMs) shouldBe toDateTimeStr(stats1.atimeMs)
      toDateTimeStr(stats2.mtimeMs) shouldBe toDateTimeStr(stats1.mtimeMs)

      fs.readFileSync(file2, new FileOptions {
        override val encoding = "utf8"
      }) shouldBe "hello, World!!!"

      //cleanup
      fs.unlinkSync(file1)
      fs.unlinkSync(file2)
      fs.rmdirSync(tmpDir)
      Succeeded
    }
  }

  it should "overwrite existing file when readFile/writeFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true

    val file1 = path.join(tmpDir, "example.txt")
    val file2 = path.join(tmpDir, "example2.txt")
    fs.writeFileSync(file1, "hello, World")
    fs.writeFileSync(file2, "hello, World!!!")
    fs.existsSync(file1) shouldBe true
    fs.existsSync(file2) shouldBe true

    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val srcItem = getFileListItem("example.txt", fs.lstatSync(file1))
    val existing = getFileListItem("example2.txt", fs.lstatSync(file2))
    val buff = new Uint8Array(5)

    def loop(source: FileSource, target: FileTarget): Future[Unit] = {
      source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
        if (bytesRead == 0) target.setAttributes(srcItem).toFuture
        else target.writeNextBytes(buff, bytesRead).toFuture.flatMap(_ => loop(source, target))
      }
    }
    
    //then
    onExists.expects(*).onCall { resItem: FileListItem =>
      assertFileListItem(resItem, existing)
      js.Promise.resolve[js.UndefOr[Boolean]](true)
    }
    
    //when
    val resultF = for {
      source <- apiImp.readFile(tmpDir, FileListItem("example.txt"), 0.0).toFuture
      maybeTarget <- apiImp.writeFile(tmpDir, "example2.txt", onExists).toFuture
      _ <- maybeTarget.map(loop(source, _)).getOrElse(Future.unit)
      _ <- maybeTarget.map(_.close().toFuture).getOrElse(Future.unit)
      _ <- source.close().toFuture
    } yield ()

    resultF.map { _ =>
      //then
      val stats2 = fs.lstatSync(file2)
      stats2.size shouldBe srcItem.size
      toDateTimeStr(stats2.atimeMs) shouldBe toDateTimeStr(srcItem.atimeMs)
      toDateTimeStr(stats2.mtimeMs) shouldBe toDateTimeStr(srcItem.mtimeMs)

      fs.readFileSync(file2, new FileOptions {
        override val encoding = "utf8"
      }) shouldBe "hello, World"

      //cleanup
      fs.unlinkSync(file1)
      fs.unlinkSync(file2)
      fs.rmdirSync(tmpDir)
      Succeeded
    }
  }

  it should "append to existing file when readFile/writeFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true

    val file1 = path.join(tmpDir, "example.txt")
    val file2 = path.join(tmpDir, "example2.txt")
    fs.writeFileSync(file1, "hello, World!!!")
    fs.writeFileSync(file2, "hello")
    fs.existsSync(file1) shouldBe true
    fs.existsSync(file2) shouldBe true

    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val srcItem = getFileListItem("example.txt", fs.lstatSync(file1))
    val existing = getFileListItem("example2.txt", fs.lstatSync(file2))
    val buff = new Uint8Array(5)

    def loop(source: FileSource, target: FileTarget): Future[Unit] = {
      source.readNextBytes(buff).toFuture.flatMap { bytesRead =>
        if (bytesRead == 0) target.setAttributes(srcItem).toFuture
        else target.writeNextBytes(buff, bytesRead).toFuture.flatMap(_ => loop(source, target))
      }
    }
    
    //then
    onExists.expects(*).onCall { resItem: FileListItem =>
      assertFileListItem(resItem, existing)
      js.Promise.resolve[js.UndefOr[Boolean]](false)
    }
    
    //when
    val resultF = for {
      source <- apiImp.readFile(tmpDir, FileListItem("example.txt"), 0).toFuture
      maybeTarget <- apiImp.writeFile(tmpDir, "example2.txt", onExists).toFuture
      _ <- maybeTarget.map(loop(source, _)).getOrElse(Future.unit)
      _ <- maybeTarget.map(_.close().toFuture).getOrElse(Future.unit)
      _ <- source.close().toFuture
    } yield maybeTarget

    resultF.flatMap { maybeTarget =>
      //then
      val stats2 = fs.lstatSync(file2)
      stats2.size shouldBe (existing.size + srcItem.size)
      toDateTimeStr(stats2.atimeMs) shouldBe toDateTimeStr(srcItem.atimeMs)
      toDateTimeStr(stats2.mtimeMs) shouldBe toDateTimeStr(srcItem.mtimeMs)

      fs.readFileSync(file2, new FileOptions {
        override val encoding = "utf8"
      }) shouldBe "hellohello, World!!!"

      //cleanup
      fs.unlinkSync(file1)
      maybeTarget.map(_.delete().toFuture).getOrElse(Future.unit).map { _ =>
        fs.rmdirSync(tmpDir)
        Succeeded
      }
    }
  }

  it should "return undefined if skip existing file when writeFile" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"))
    fs.existsSync(tmpDir) shouldBe true

    val file = path.join(tmpDir, "example2.txt")
    fs.writeFileSync(file, "hello")
    fs.existsSync(file) shouldBe true

    val onExists = mockFunction[FileListItem, js.Promise[js.UndefOr[Boolean]]]
    val existing = getFileListItem("example2.txt", fs.lstatSync(file))

    //then
    onExists.expects(*).onCall { resItem: FileListItem =>
      assertFileListItem(resItem, existing)
      js.Promise.resolve[js.UndefOr[Boolean]](js.undefined)
    }
    
    //when
    val resultF = apiImp.writeFile(tmpDir, "example2.txt", onExists).toFuture

    resultF.flatMap { maybeTarget =>
      //then
      maybeTarget shouldBe js.undefined

      //cleanup
      fs.unlinkSync(file)
      fs.rmdirSync(tmpDir)
      Succeeded
    }
  }

  it should "call fsService.readDisk when getDriveRoot" in {
    //given
    val fsService = new FsService
    val apiImp = new FSFileListApi(fsService = fsService.fsService)
    val path = "test path"
    val drive = FSDisk("/some/path", 0, 0, "SomeDrive")

    //then
    fsService.readDisk.expects(path).returning(js.Promise.resolve[js.UndefOr[FSDisk]](drive))

    //when
    val resultF = apiImp.getDriveRoot(path).toFuture

    //then
    resultF.map { result =>
      result shouldBe drive.root
    }
  }

  it should "return file permissions" in {
    //given
    def flag(s: Char, c: Char, f: Int): Int = {
      if (s == c) f else 0
    }
    
    def of(s: String): Int = {
      (flag(s(0), 'd', FSConstants.S_IFDIR)
        | flag(s(1), 'r', FSConstants.S_IRUSR.getOrElse(0))
        | flag(s(2), 'w', FSConstants.S_IWUSR.getOrElse(0))
        | flag(s(3), 'x', FSConstants.S_IXUSR.getOrElse(0))
        | flag(s(4), 'r', FSConstants.S_IRGRP.getOrElse(0))
        | flag(s(5), 'w', FSConstants.S_IWGRP.getOrElse(0))
        | flag(s(6), 'x', FSConstants.S_IXGRP.getOrElse(0))
        | flag(s(7), 'r', FSConstants.S_IROTH.getOrElse(0))
        | flag(s(8), 'w', FSConstants.S_IWOTH.getOrElse(0))
        | flag(s(9), 'x', FSConstants.S_IXOTH.getOrElse(0)))
    }
    
    def expected(s: String): String = {
      if (process.platform == Platform.win32) s"${s.take(3)}-------"
      else s
    }
    
    //when & then
    FSFileListApi.getPermissions(0) shouldBe "----------"
    FSFileListApi.getPermissions(of("d---------")) shouldBe expected("d---------")
    FSFileListApi.getPermissions(of("-r--------")) shouldBe expected("-r--------")
    FSFileListApi.getPermissions(of("--w-------")) shouldBe expected("--w-------")
    FSFileListApi.getPermissions(of("---x------")) shouldBe expected("---x------")
    FSFileListApi.getPermissions(of("----r-----")) shouldBe expected("----r-----")
    FSFileListApi.getPermissions(of("-----w----")) shouldBe expected("-----w----")
    FSFileListApi.getPermissions(of("------x---")) shouldBe expected("------x---")
    FSFileListApi.getPermissions(of("-------r--")) shouldBe expected("-------r--")
    FSFileListApi.getPermissions(of("--------w-")) shouldBe expected("--------w-")
    FSFileListApi.getPermissions(of("---------x")) shouldBe expected("---------x")
    FSFileListApi.getPermissions(of("drwxrwxrwx")) shouldBe expected("drwxrwxrwx")
    
    Succeeded
  }

  private def getFileListItem(name: String, stats: Stats) = {
    val isDir = stats.isDirectory()
    FileListItem.copy(FileListItem(name, isDir))(
      isSymLink = stats.isSymbolicLink(),
      size = if (isDir) 0.0 else stats.size,
      atimeMs = stats.atimeMs,
      mtimeMs = stats.mtimeMs,
      ctimeMs = stats.ctimeMs,
      birthtimeMs = stats.birthtimeMs,
      permissions = FSFileListApi.getPermissions(stats.mode)
    )
  }

  private def toDateTimeStr(dtimeMs: Double): String = {
    val date = new js.Date(dtimeMs)
    s"${date.toLocaleDateString()} ${date.toLocaleTimeString()}"
  }

  private def del(path: String, isDir: Boolean): Unit = {
    if (fs.existsSync(path)) {
      if (isDir) fs.rmdirSync(path)
      else fs.unlinkSync(path)
    }
  }
}
