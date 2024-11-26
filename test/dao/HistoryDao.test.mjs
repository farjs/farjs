/**
 * @typedef {import("../../dao/HistoryDao.mjs").History} History
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

const testItem = "test/item";
const params = {
  isEdit: false,
  encoding: "test-encoding",
  position: 123,
  wrap: true,
  column: 4,
};

describe("HistoryDao.test.mjs", () => {
  it("should create new records when save", async () => {
    //given
    const db = await testDb();
    const kindDao = HistoryKindDao(db);
    const maxItemsCount = 10;
    const dao0 = HistoryDao(
      db,
      { id: -1, name: "non-existing-kind" },
      maxItemsCount
    );
    await dao0.deleteAll();
    await kindDao.deleteAll();
    assert.deepEqual((await dao0.getByItem("test")) == undefined, true);

    //when & then
    const kind1 = await kindDao.upsert({ id: -1, name: "test_kind1" });
    const dao1 = HistoryDao(db, kind1, maxItemsCount);
    const entity1 = getHistory(testItem, params);
    await dao1.save(entity1, Date.now());
    assert.deepEqual(await dao1.getByItem(entity1.item), entity1);

    //when & then
    const kind2 = await kindDao.upsert({ id: -1, name: "test_kind2" });
    const dao2 = HistoryDao(db, kind2, maxItemsCount);
    const entity2 = getHistory(testItem);
    await dao2.save(entity2, Date.now());
    assert.deepEqual(await dao2.getByItem(entity2.item), entity2);

    //then
    const results1 = await dao1.getAll();
    const results2 = await dao2.getAll();

    assert.deepEqual(results1, [entity1]);
    assert.deepEqual(results2, [entity2]);
  });

  it("should update existing record when save", async () => {
    //given
    const db = await testDb();
    const kindDao = HistoryKindDao(db);
    const maxItemsCount = 10;
    const updatedParams = {
      ...params,
      encoding: "updated-encoding",
      position: 456,
      wrap: false,
      column: 5,
    };

    const allKinds = await kindDao.getAll();
    const [kind1, kind2] = allKinds;
    const dao1 = HistoryDao(db, kind1, maxItemsCount);
    const dao2 = HistoryDao(db, kind2, maxItemsCount);
    const all2 = await dao2.getAll();
    const [entity2] = all2;

    //when
    const updated = getHistory(testItem, updatedParams);
    await dao1.save(updated, Date.now());

    //then
    const results1 = await dao1.getAll();
    const results2 = await dao2.getAll();

    assert.deepEqual(results1, [updated]);
    assert.deepEqual(results2, [entity2]);
  });

  it("should keep last N records when save", async () => {
    //given
    const db = await testDb();
    const kindDao = HistoryKindDao(db);
    const maxItemsCount = 3;

    const allKinds = await kindDao.getAll();
    const [kind1, kind2] = allKinds;
    const dao1 = HistoryDao(db, kind1, maxItemsCount);
    const dao2 = HistoryDao(db, kind2, maxItemsCount);
    const updatedAt = Date.now();

    //when
    for (let i = 1; i <= 5; i += 1) {
      await dao1.save(getHistory(`${testItem}${i}`), updatedAt + i);
    }

    //then
    const results1 = await dao1.getAll();
    const results2 = await dao2.getAll();

    assert.deepEqual(
      results1.map((_) => _.item),
      ["test/item3", "test/item4", "test/item5"]
    );
    assert.deepEqual(
      results2.map((_) => _.item),
      ["test/item"]
    );
  });
});

/**
 * @param {string} item
 * @param {object} [params]
 * @returns {History}
 */
function getHistory(item, params) {
  return { item, params };
}
