/**
 * @typedef {import("../../file/FilePluginUi.mjs").FilePluginUiParams} FilePluginUiParams
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FilePlugin from "../../file/FilePlugin.mjs";
import FilePluginLoader from "../../file/FilePluginLoader.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fsComp = () => null;

const stack = new PanelStack(
  true,
  [new PanelStackItem(fsComp)],
  mockFunction(),
);
const stacks = WithStacksProps(WithStacksData(stack), WithStacksData(stack));

describe("FilePlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["M-v"];

    //when & then
    assert.deepEqual(FilePluginLoader.triggerKeys, expected);
  });

  it("should return undefined if non-trigger key when onKeyTrigger", async () => {
    //when
    const result = await FilePluginLoader.onKeyTrigger("test_key", stacks);

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return ui component if trigger key when onKeyTrigger", async () => {
    //when
    const result = await FilePluginLoader.onKeyTrigger("M-v", stacks);

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
