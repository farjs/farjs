package farjs.text

import farjs.text.raw.Iconv
import scommons.nodejs.Buffer

object Encoding {
  
  lazy val encodings: List[String] = {
    //TODO: load encodings from iconv-lite, see:
    //  https://github.com/ashtuchkin/iconv-lite/issues/289
    List(
      "base64",
      "big5",
      "chinese",
      "cp866",
      "cyrillic",
      "hex",
      "koi8r",
      "koi8ru",
      "koi8t",
      "koi8u",
      "korean",
      "latin1",
      "mac",
      "maccenteuro",
      "maccroatian",
      "maccyrillic",
      "macgreek",
      "maciceland",
      "macintosh",
      "macroman",
      "macromania",
      "macthai",
      "macturkish",
      "macukraine",
      "msansi",
      "msarab",
      "mscyrl",
      "msee",
      "msgreek",
      "mshebr",
      "msturk",
      "utf16",
      "utf16be",
      "utf16le",
      "utf32",
      "utf32be",
      "utf32le",
      "utf7",
      "utf7imap",
      "utf8",
      "win1250",
      "win1251",
      "win1252",
      "win1253",
      "win1254",
      "win1255",
      "win1256",
      "win1257",
      "win1258"
    )
  }

  def decode(buf: Buffer, encoding: String, start: Int, end: Int): String = {
    if (Buffer.isEncoding(encoding)) {
      buf.toString(encoding, start, end)
    }
    else {
      Iconv.decode(buf.subarray(start, end), encoding)
    }
  }

  def byteLength(string: String, encoding: String): Int = {
    if (Buffer.isEncoding(encoding)) {
      Buffer.byteLength(string, encoding)
    }
    else {
      //TODO: improve performance, see:
      //  https://github.com/ashtuchkin/iconv-lite/issues/308
      Iconv.encode(string, encoding).length
    }
  }
}
