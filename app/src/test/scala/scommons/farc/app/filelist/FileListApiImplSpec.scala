package scommons.farc.app.filelist

import org.scalatest.Succeeded
import scommons.farc.api.filelist._
import scommons.nodejs._
import scommons.nodejs.raw.FSConstants
import scommons.nodejs.test.AsyncTestSpec

class FileListApiImplSpec extends AsyncTestSpec {
  
  private val apiImp = new FileListApiImpl

  it should "return current directory" in {
    //when
    val resultF = apiImp.currDir
    
    //then
    resultF.map { dir =>
      inside(dir) { case FileListDir(dirPath, isRoot) =>
        dirPath shouldBe process.cwd()
        isRoot shouldBe false
      }
    }
  }
  
  it should "change current directory" in {
    //given
    val curr = process.cwd()
    val newDir = os.homedir()
    val newDirObj = path.parse(newDir)
    val newRoot = newDirObj.root.getOrElse("")
    val newParent = newDirObj.dir.getOrElse("")
    newDir should not be curr
    newParent should not be empty

    //when
    val resultF = (for {
      dir <- apiImp.changeDir(newDir)
      currDir <- apiImp.currDir
    } yield {
      //then
      process.cwd() shouldBe newDir
      dir shouldBe currDir
      inside(dir) { case FileListDir(dirPath, isRoot) =>
        dirPath shouldBe newDir
        isRoot shouldBe false
      }
    }).flatMap { _ =>
      //when
      (for {
        dir <- apiImp.changeDir(FileListItem.up.name)
        currDir <- apiImp.currDir
      } yield {
        //then
        process.cwd() shouldBe newParent
        dir shouldBe currDir
        inside(dir) { case FileListDir(dirPath, isRoot) =>
          dirPath shouldBe newParent
          isRoot shouldBe false
        }
      }).flatMap { _ =>
        //when
        for {
          dir <- apiImp.changeDir(newRoot)
          currDir <- apiImp.currDir
        } yield {
          //then
          process.cwd() shouldBe newRoot
          dir shouldBe currDir
          inside(dir) { case FileListDir(dirPath, isRoot) =>
            dirPath shouldBe newRoot
            isRoot shouldBe true
          }
        }
      }
    }
    
    //cleanup
    resultF.flatMap(_ => apiImp.changeDir(curr).map(_ => Succeeded))
  }
  
  it should "return file list when listFiles()" in {
    //given
    val curr = os.homedir()
    
    val resultF = apiImp.changeDir(curr).flatMap { _ =>
      //when
      apiImp.listFiles.map { files =>
        //then
        files should not be empty
      }
    }
    
    //cleanup
    resultF.flatMap(_ => apiImp.changeDir(curr).map(_ => Succeeded))
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
