/**
 * @typedef {import("../../file/MockFileReader.mjs").FileReaderMocks} FileReaderMocks
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockFileReader from "../../file/MockFileReader.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("MockFileReader.test.mjs", () => {
  it("should construct instance with default props", async () => {
    //given
    /** @param {Promise<?>} p */
    async function checkRejected(p) {
      let error = null;
      try {
        //when
        await p;
      } catch (e) {
        error = e;
      }

      //then
      assert.deepEqual(error, Error("Not implemented!"));
    }

    //when
    const result = new MockFileReader();

    //then
    await checkRejected(result.open("test"));
    await checkRejected(result.close());
    await checkRejected(result.readBytes(0, Buffer.from([])));
  });

  it("should construct instance with mocks", () => {
    //given
    /** @type {FileReaderMocks} */
    const mocks = {
      open: mockFunction(),
      close: mockFunction(),
      readBytes: mockFunction(),
    };

    //when
    const result = new MockFileReader(mocks);

    //then
    assert.deepEqual(result.open === mocks.open, true);
    assert.deepEqual(result.close === mocks.close, true);
    assert.deepEqual(result.readBytes === mocks.readBytes, true);
  });
});
