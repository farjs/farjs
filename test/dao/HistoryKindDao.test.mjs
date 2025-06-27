/**
 * @typedef {import("../../dao/HistoryKindDao.mjs").HistoryKindEntity} HistoryKindEntity
 */
import assert from "node:assert/strict";
import testDb from "../db.mjs";
import HistoryDao from "../../dao/HistoryDao.mjs";
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
  it("should create new record when upsert", async () => {
    //given
    const db = await testDb();
    const dao = HistoryKindDao(db);
    const entity = getHistoryKindEntity();
    const dao0 = HistoryDao(db, { id: -1, name: "non-existing-kind" }, 10);
    await dao0.deleteAll();
    await dao.deleteAll();

    //when
    const res = await dao.upsert(entity);

    //then
    const results = await dao.getAll();

    assert.deepEqual(res.id > 0, true);
    assert.deepEqual({ ...res }, { ...entity, id: res.id });
    assert.deepEqual(results, [res]);
  });

  it("should return existing record when upsert", async () => {
    //given
    const db = await testDb();
    const dao = HistoryKindDao(db);
    const existing = (await dao.getAll())[0];
    const entity = getHistoryKindEntity(existing.name);

    //when
    const res = await dao.upsert(entity);

    //then
    const results = await dao.getAll();

    assert.deepEqual({ ...res }, { ...entity, id: existing.id });
    assert.deepEqual(results, [res]);
  });
});

/**
 * @param {string} name
 * @returns {HistoryKindEntity}
 */
function getHistoryKindEntity(name = "test_history_kind") {
  return { id: -1, name };
}
