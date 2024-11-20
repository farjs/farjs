/**
 * @typedef {import("../../dao/HistoryKindDao.mjs").HistoryKindEntity} HistoryKindEntity
 */
import assert from "node:assert/strict";
import HistoryKindDao from "../../dao/HistoryKindDao.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("HistoryKindDao.test.mjs", () => {
  it("should return all entities when getAll", async () => {
    //given

    //when
    const results = await HistoryKindDao.getAll();

    //then
    assert.deepEqual(results, []);
  });
});
