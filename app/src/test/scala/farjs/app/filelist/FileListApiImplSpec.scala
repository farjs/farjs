package farjs.app.filelist

import farjs.api.filelist._
import org.scalatest.Succeeded
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec

class FileListApiImplSpec extends AsyncTestSpec {
  
  private val apiImp = new FileListApiImpl

  it should "return current dir info and files when readDir(None, .)" in {
    //when
    apiImp.readDir(None, FileListDir.curr).map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe process.cwd()
        isRoot shouldBe false
        items should not be empty
      }
    }
  }
  
  it should "return parent dir info and files when readDir(Some(dir), ..)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val parentDir = currDirObj.dir.getOrElse("")
    val currDir = currDirObj.base.getOrElse("")
    parentDir should not be empty
    currDir should not be empty

    //when
    apiImp.readDir(Some(curr), FileListItem.up.name).map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe parentDir
        isRoot shouldBe false
        items should not be empty
      }
    }
  }
  
  it should "return target dir info and files when readDir(Some(dir), sub-dir)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val parentDir = currDirObj.dir.getOrElse("")
    val subDir = currDirObj.base.getOrElse("")
    parentDir should not be empty
    subDir should not be empty

    //when
    apiImp.readDir(Some(parentDir), subDir).map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe curr
        isRoot shouldBe false
        items should not be empty
      }
    }
  }
  
  it should "return root dir info and files when readDir(Some(root), .)" in {
    //given
    val curr = process.cwd()
    val currDirObj = path.parse(curr)
    val currRoot = currDirObj.root.getOrElse("")
    currRoot should not be empty

    //when
    apiImp.readDir(Some(currRoot), FileListDir.curr).map { dir =>
      //then
      inside(dir) { case FileListDir(dirPath, isRoot, items) =>
        dirPath shouldBe currRoot
        isRoot shouldBe true
        items should not be empty
      }
    }
  }
  
  it should "delete items when delete" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "far-js-test-"))
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
    val items = List(
      FileListItem(d1Name, isDir = true),
      FileListItem(f1Name)
    )

    //when
    val resultF = apiImp.delete(tmpDir, items)

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
  
  it should "create multiple directories when mkDir" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "far-js-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val dir = s"test1${path.sep}test2${path.sep}${path.sep}test3${path.sep}"

    //when
    val resultF = apiImp.mkDir(tmpDir, dir, multiple = true)

    //then
    val resCheckF = resultF.map { _ =>
      fs.existsSync(path.join(tmpDir, dir)) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(path.join(tmpDir, dir), isDir = true)
      del(path.join(tmpDir, "test1", "test2"), isDir = true)
      del(path.join(tmpDir, "test1"), isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "create single directory when mkDir" in {
    //given
    val tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "far-js-test-"))
    fs.existsSync(tmpDir) shouldBe true
    val dir = "test123"

    //when
    val resultF = apiImp.mkDir(tmpDir, dir, multiple = false)

    //then
    val resCheckF = resultF.map { _ =>
      fs.existsSync(path.join(tmpDir, dir)) shouldBe true
    }

    //cleanup
    resCheckF.map { _ =>
      del(path.join(tmpDir, dir), isDir = true)
      del(tmpDir, isDir = true)
      Succeeded
    }
  }
  
  it should "return file permissions" in {
    //given
    def flag(s: Char, c: Char, f: Int): Int = {
      if (s == c) f else 0
    }
    
    def of(s: String): Int = {
      (flag(s(0), 'd', FSConstants.S_IFDIR)
        | flag(s(1), 'r', FSConstants.S_IRUSR)
        | flag(s(2), 'w', FSConstants.S_IWUSR)
        | flag(s(3), 'x', FSConstants.S_IXUSR)
        | flag(s(4), 'r', FSConstants.S_IRGRP)
        | flag(s(5), 'w', FSConstants.S_IWGRP)
        | flag(s(6), 'x', FSConstants.S_IXGRP)
        | flag(s(7), 'r', FSConstants.S_IROTH)
        | flag(s(8), 'w', FSConstants.S_IWOTH)
        | flag(s(9), 'x', FSConstants.S_IXOTH))
    }
    
    //when & then
    apiImp.getPermissions(0) shouldBe "----------"
    apiImp.getPermissions(of("d---------")) shouldBe "d---------"
    apiImp.getPermissions(of("-r--------")) shouldBe "-r--------"
    apiImp.getPermissions(of("--w-------")) shouldBe "--w-------"
    apiImp.getPermissions(of("---x------")) shouldBe "---x------"
    apiImp.getPermissions(of("----r-----")) shouldBe "----r-----"
    apiImp.getPermissions(of("-----w----")) shouldBe "-----w----"
    apiImp.getPermissions(of("------x---")) shouldBe "------x---"
    apiImp.getPermissions(of("-------r--")) shouldBe "-------r--"
    apiImp.getPermissions(of("--------w-")) shouldBe "--------w-"
    apiImp.getPermissions(of("---------x")) shouldBe "---------x"
    apiImp.getPermissions(of("drwxrwxrwx")) shouldBe "drwxrwxrwx"
    
    Succeeded
  }

  private def del(path: String, isDir: Boolean): Unit = {
    if (fs.existsSync(path)) {
      if (isDir) fs.rmdirSync(path)
      else fs.unlinkSync(path)
    }
  }
}
