package scommons.nodejs

import scommons.nodejs.test.AsyncTestSpec

import scala.scalajs.js.JavaScriptException

class FSSpec extends AsyncTestSpec {

  it should "fail if no such dir when readdir()" in {
    //given
    val dir = s"${os.homedir()}-unknown"
    
    //when
    val result = fs.readdir(dir)
    
    //then
    result.failed.map {
      case JavaScriptException(error) =>
        error.toString should include ("no such file or directory")
    }
  }
  
  it should "return list of files when readdir()" in {
    //when & then
    fs.readdir(os.homedir()).map { files =>
      files should not be empty
    }
  }
  
  it should "return stats when lstatSync()" in {
    //when
    val stats = fs.lstatSync(os.homedir())
    
    //then
    stats.isDirectory shouldBe true
    stats.isFile() shouldBe false
    stats.isSymbolicLink() shouldBe false
    
    stats.size should be > 0.0
    
    stats.atimeMs should be > 0.0
    stats.mtimeMs should be > 0.0
    stats.ctimeMs should be > 0.0
    stats.birthtimeMs should be > 0.0
  }
}
