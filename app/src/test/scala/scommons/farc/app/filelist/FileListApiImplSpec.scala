package scommons.farc.app.filelist

import org.scalatest.Succeeded
import scommons.farc.api.filelist.FileListItem
import scommons.nodejs._
import scommons.nodejs.test.AsyncTestSpec

class FileListApiImplSpec extends AsyncTestSpec {
  
  private val apiImp = new FileListApiImpl

  it should "return root directory" in {
    //given
    val root = path.parse(process.cwd()).root.getOrElse("")

    //when & then
    apiImp.rootDir shouldBe root
  }
  
  it should "change current directory" in {
    //given
    val curr = process.cwd()
    val newDir = os.homedir()
    val newParent = path.parse(newDir).dir.getOrElse("")
    newDir should not be curr
    newParent should not be empty

    //when
    val resultF = apiImp.changeDir(newDir).flatMap { dir =>
      //then
      dir shouldBe newDir
      process.cwd() shouldBe newDir
    
      //when
      apiImp.changeDir(FileListItem.up.name).map { dir =>
        //then
        dir shouldBe newParent
        process.cwd() shouldBe newParent
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
}
