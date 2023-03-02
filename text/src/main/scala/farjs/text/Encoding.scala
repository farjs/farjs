package farjs.text

import farjs.text.raw.Iconv
import scommons.nodejs.Process.Platform
import scommons.nodejs.{Buffer, process}

object Encoding {
  
  private[text] val winDefault = "win1251"
  private[text] val unixDefault = "utf8"

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
      unixDefault,
      "win1250",
      winDefault,
      "win1252",
      "win1253",
      "win1254",
      "win1255",
      "win1256",
      "win1257",
      "win1258"
    )
  }
  
  lazy val platformEncoding: String = getByPlatform(process.platform)

  private[text] def getByPlatform(platform: Platform): String = {
    if (platform == Platform.win32) winDefault
    else unixDefault
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
