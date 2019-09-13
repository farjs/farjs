package farclone.app.filelist

import org.scalatest.Succeeded
import farclone.api.filelist._
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
}
