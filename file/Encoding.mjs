import iconv from "iconv-lite";

const winDefault = "win1251";
const unixDefault = "utf8";

/**
 * @param {NodeJS.Platform} platform
 * @returns {string}
 */
function getByPlatform(platform) {
  return platform === "win32" ? winDefault : unixDefault;
}

const Encoding = Object.freeze({
  /** @type {string} */
  platformEncoding: getByPlatform(process.platform),

  _getByPlatform: getByPlatform,

  /**
   * @param {Buffer} buf
   * @param {string} encoding
   * @param {number} start
   * @param {number} end
   */
  decode: (buf, encoding, start, end) => {
    if (Buffer.isEncoding(encoding)) {
      return buf.toString(encoding, start, end);
    }

    return iconv.decode(buf.subarray(start, end), encoding);
  },

  /**
   * @param {string} str
   * @param {string} encoding
   * @returns {number}
   */
  byteLength: (str, encoding) => {
    if (Buffer.isEncoding(encoding)) {
      return Buffer.byteLength(str, encoding);
    }

    //TODO: improve performance, see:
    //  https://github.com/ashtuchkin/iconv-lite/issues/308
    return iconv.encode(str, encoding).length;
  },

  //TODO: load encodings from iconv-lite, see:
  //  https://github.com/ashtuchkin/iconv-lite/issues/289
  encodings: Object.freeze([
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
    "win1258",
  ]),
});

export default Encoding;
