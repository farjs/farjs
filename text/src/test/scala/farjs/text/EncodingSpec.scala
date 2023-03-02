package farjs.text

import farjs.text.Encoding.{unixDefault, winDefault}
import farjs.text.raw.Iconv
import scommons.nodejs.{Buffer, process}
import scommons.nodejs.Process.Platform
import scommons.nodejs.test._

class EncodingSpec extends TestSpec {

  it should "return list of supported encodings" in {
    //when & then
    Encoding.encodings should contain (unixDefault)
    Encoding.encodings should contain (winDefault)
  }

  it should "return platform encoding" in {
    //when & then
    Encoding.platformEncoding shouldBe Encoding.getByPlatform(process.platform)

    //when & then
    Encoding.getByPlatform(Platform.win32) shouldBe winDefault
    Encoding.getByPlatform(Platform.darwin) shouldBe unixDefault
    Encoding.getByPlatform(Platform.linux) shouldBe unixDefault
    Encoding.getByPlatform(Platform.aix) shouldBe unixDefault
  }

  it should "support Buffer encoding when decode" in {
    //given
    val str = "Straße"
    val encoding = "utf8"
    val buf = Buffer.from(str, encoding)

    //when & then
    Encoding.decode(buf, encoding, start = 0, end = buf.length) shouldBe str
  }

  it should "support non-Buffer encoding when decode" in {
    //given
    val str = "Straße"
    val encoding = "win1252"
    val buf = Iconv.encode(str, encoding)

    //when & then
    Encoding.decode(buf, encoding, start = 0, end = buf.length) shouldBe str
  }

  it should "return length in bytes for Buffer encoding when byteLength" in {
    //given
    val encoding = "utf8"

    //when & then
    Encoding.byteLength(string = "Straße", encoding) shouldBe 7
  }

  it should "return length in bytes for non-Buffer encoding when byteLength" in {
    //given
    val encoding = "win1252"
    
    //when & then
    Encoding.byteLength(string = "Straße", encoding) shouldBe 6
  }
}
