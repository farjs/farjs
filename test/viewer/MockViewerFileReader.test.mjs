/**
 * @typedef {import("../../viewer/MockViewerFileReader.mjs").ViewerFileReaderMocks} ViewerFileReaderMocks
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockViewerFileReader from "../../viewer/MockViewerFileReader.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("MockViewerFileReader.test.mjs", () => {
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
    const result = new MockViewerFileReader();

    //then
    await checkRejected(result.open("test"));
    await checkRejected(result.close());
    await checkRejected(result.readPrevLines(0, 0, 0, "utf-8"));
    await checkRejected(result.readNextLines(0, 0, "utf-8"));
  });

  it("should construct instance with mocks", () => {
    //given
    /** @type {ViewerFileReaderMocks} */
    const mocks = {
      open: mockFunction(),
      close: mockFunction(),
      readPrevLines: mockFunction(),
      readNextLines: mockFunction(),
    };

    //when
    const result = new MockViewerFileReader(mocks);

    //then
    assert.deepEqual(result.open === mocks.open, true);
    assert.deepEqual(result.close === mocks.close, true);
    assert.deepEqual(result.readPrevLines === mocks.readPrevLines, true);
    assert.deepEqual(result.readNextLines === mocks.readNextLines, true);
  });
});
