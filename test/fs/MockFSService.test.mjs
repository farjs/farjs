/**
 * @import { FSServiceMocks } from "../../fs/MockFSService.mjs"
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockFSService from "../../fs/MockFSService.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("MockFSService.test.mjs", () => {
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
    const result = new MockFSService();

    //then
    await checkRejected(result.openItem("test1", "test2"));
    await checkRejected(result.readDisk("test"));
    await checkRejected(result.readDisks());
  });

  it("should construct instance with mocks", () => {
    //given
    /** @type {FSServiceMocks} */
    const mocks = {
      openItem: mockFunction(),
      readDisk: mockFunction(),
      readDisks: mockFunction(),
    };

    //when
    const result = new MockFSService(mocks);

    //then
    assert.deepEqual(result.openItem === mocks.openItem, true);
    assert.deepEqual(result.readDisk === mocks.readDisk, true);
    assert.deepEqual(result.readDisks === mocks.readDisks, true);
  });
});
