package scommons.nodejs

import scommons.nodejs.test.TestSpec

class OSSpec extends TestSpec {

  it should "return user home dir when homedir()" in {
    //when & then
    os.homedir() should not be empty
  }
}
