import { deepEqual } from "node:assert/strict";
import testDb from "../../db.mjs";
import FileListModule from "../../../app/filelist/FileListModule.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FileListModule.test.mjs", () => {
  it("should create new instance with dependencies", async () => {
    //given
    const db = await testDb();

    //when
    const result = new FileListModule(db);

    //then
    deepEqual(result.fsServices !== undefined, true);
    deepEqual(result.historyProvider !== undefined, true);
  });
});
