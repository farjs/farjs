import assert from "node:assert/strict";
import iconv from "iconv-lite";
import Encoding from "../../file/Encoding.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("Encoding.test.mjs", () => {
  it("should return list of supported encodings", () => {
    //when & then
    assert.deepEqual(Encoding.encodings.indexOf("win1251") >= 0, true);
    assert.deepEqual(Encoding.encodings.indexOf("utf8") >= 0, true);
  });

  it("should return platform encoding", () => {
    //when & then
    assert.deepEqual(
      Encoding.platformEncoding,
      Encoding._getByPlatform(process.platform)
    );

    //when & then
    assert.deepEqual(Encoding._getByPlatform("win32"), "win1251");
    assert.deepEqual(Encoding._getByPlatform("darwin"), "utf8");
    assert.deepEqual(Encoding._getByPlatform("linux"), "utf8");
    assert.deepEqual(Encoding._getByPlatform("aix"), "utf8");
  });

  it("should support Buffer encoding when decode", () => {
    //given
    const str = "Straße";
    const encoding = "utf8";
    const buf = Buffer.from(str, encoding);

    //when & then
    assert.deepEqual(Encoding.decode(buf, encoding, 0, buf.length), str);
  });

  it("should support non-Buffer encoding when decode", () => {
    //given
    const str = "Straße";
    const encoding = "win1252";
    const buf = iconv.encode(str, encoding);

    //when & then
    assert.deepEqual(Encoding.decode(buf, encoding, 0, buf.length), str);
  });

  it("should return length in bytes for Buffer encoding when byteLength", () => {
    //given
    const encoding = "utf8";

    //when & then
    assert.deepEqual(Encoding.byteLength("Straße", encoding), 7);
  });

  it("should return length in bytes for non-Buffer encoding when byteLength", () => {
    //given
    const encoding = "win1252";

    //when & then
    assert.deepEqual(Encoding.byteLength("Straße", encoding), 6);
  });
});
