package farjs.text

import farjs.text.raw.Iconv
import scommons.nodejs.Buffer
import scommons.nodejs.test._

class EncodingSpec extends TestSpec {

  it should "return list of supported encodings" in {
    //when & then
    Encoding.encodings should contain ("utf8")
    Encoding.encodings should contain ("win1252")
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
