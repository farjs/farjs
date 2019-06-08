package scommons.nodejs

import scommons.nodejs.test.AsyncTestSpec

class FSSpec extends AsyncTestSpec {

  it should "return list of files when readdir()" in {
    //when & then
    fs.readdir(new URL(s"file://${os.homedir()}")).map { files =>
      files should not be empty
    }
  }
  
  it should "return stats when lstatSync()" in {
    //when
    val stats = fs.lstatSync(new URL(s"file://${os.homedir()}"))
    
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
