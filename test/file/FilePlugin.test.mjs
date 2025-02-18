/**
 * @typedef {import("../../file/FilePluginUi.mjs").FilePluginUiParams} FilePluginUiParams
 */
import assert from "node:assert/strict";
import FilePlugin from "../../file/FilePlugin.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FilePlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["M-v"];

    //when & then
    assert.deepEqual(FilePlugin.triggerKeys, expected);
  });

  it("should return undefined if non-trigger key when onKeyTrigger", async () => {
    //when
    const result = await FilePlugin.onKeyTrigger("test_key");

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return ui component if trigger key when onKeyTrigger", async () => {
    //when
    const result = await FilePlugin.onKeyTrigger("M-v");

    //then
    assert.deepEqual(result !== undefined, true);
  });

  it("should handle trigger key (M-v) when _createUiParams", () => {
    //when & then
    assert.deepEqual(FilePlugin._createUiParams("M-v"), {
      showFileViewHistoryPopup: true,
    });
  });
});
