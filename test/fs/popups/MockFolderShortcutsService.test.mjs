/**
 * @import { FolderShortcutsServiceMocks } from "../../../fs/popups/MockFolderShortcutsService.mjs"
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockFolderShortcutsService from "../../../fs/popups/MockFolderShortcutsService.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("MockFolderShortcutsService.test.mjs", () => {
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
    const result = new MockFolderShortcutsService();

    //then
    await checkRejected(result.getAll());
    await checkRejected(result.save(123, "test"));
    await checkRejected(result.delete(123));
  });

  it("should construct instance with mocks", () => {
    //given
    /** @type {FolderShortcutsServiceMocks} */
    const mocks = {
      getAll: mockFunction(),
      save: mockFunction(),
      delete: mockFunction(),
    };

    //when
    const result = new MockFolderShortcutsService(mocks);

    //then
    assert.deepEqual(result.getAll === mocks.getAll, true);
    assert.deepEqual(result.save === mocks.save, true);
    assert.deepEqual(result.delete === mocks.delete, true);
  });
});
