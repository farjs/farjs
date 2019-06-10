package scommons.nodejs

import scommons.nodejs.test.TestSpec

class PathSpec extends TestSpec {

  it should "check if POSIX path isAbsolute" in {
    //when & then
    path.posix.isAbsolute("/foo/bar") shouldBe true
    path.posix.isAbsolute("/baz/..") shouldBe true
    path.posix.isAbsolute("qux/") shouldBe false
    path.posix.isAbsolute(".") shouldBe false
  }
  
  it should "check if Windows path isAbsolute" in {
    //when & then
    path.win32.isAbsolute("//server") shouldBe true
    path.win32.isAbsolute("\\\\server") shouldBe true
    path.win32.isAbsolute("C:/foo/..") shouldBe true
    path.win32.isAbsolute("C:\\foo\\..") shouldBe true
    path.win32.isAbsolute("bar\\baz") shouldBe false
    path.win32.isAbsolute("bar/baz") shouldBe false
    path.win32.isAbsolute(".") shouldBe false
  }
  
  it should "join paths" in {
    //when & then
    path.posix.join("test") shouldBe "test"
    path.posix.join("test/") shouldBe "test/"
    path.posix.join("/", "test", "/") shouldBe "/test/"
    path.posix.join("a", "b") shouldBe "a/b"
  }
}
