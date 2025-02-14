/**
 * @typedef {import("../../file/FileEvent.mjs").FileEvent} FileEvent
 */
import assert from "node:assert/strict";
import FileEvent from "../../file/FileEvent.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FileEvent.test.mjs", () => {
  it("should define FileEvent enum", () => {
    //when & then
    assert.deepEqual(FileEvent, {
      onFileView: "onFileView",
    });
  });
});
