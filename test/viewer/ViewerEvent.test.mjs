/**
 * @typedef {import("../../viewer/ViewerEvent.mjs").ViewerEvent} ViewerEvent
 */
import assert from "node:assert/strict";
import ViewerEvent from "../../viewer/ViewerEvent.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("ViewerEvent.test.mjs", () => {
  it("should define ViewerEvent enum", () => {
    //when & then
    assert.deepEqual(ViewerEvent, {
      onViewerOpenLeft: "onViewerOpenLeft",
      onViewerOpenRight: "onViewerOpenRight",
    });
  });
});
