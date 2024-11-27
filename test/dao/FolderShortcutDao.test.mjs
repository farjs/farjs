/**
 * @typedef {import("../../dao/FolderShortcutDao.mjs").FolderShortcut} FolderShortcut
 */
import assert from "node:assert/strict";
import testDb from "../db.mjs";
import FolderShortcutDao from "../../dao/FolderShortcutDao.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FolderShortcutDao.test.mjs", () => {
  it("should create new record when save", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    await dao.deleteAll();
    assert.deepEqual(await dao.getAll(), []);
    const entity = getFolderShortcut(1, "test/path");

    //when
    await dao.save(entity);

    //then
    const results = await dao.getAll();
    assert.deepEqual(results, [entity]);
  });

  it("should update existing record when save", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    const [existing] = await dao.getAll();
    const entity = getFolderShortcut(existing.id, `${existing.path}_updated`);
    assert.notDeepEqual(existing, entity);

    //when
    await dao.save(entity);

    //then
    const results = await dao.getAll();
    assert.deepEqual(results, [entity]);
  });

  it("should delete existing record when delete", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    const [existing] = await dao.getAll();
    const entity = getFolderShortcut(existing.id + 1, "test/path2");
    await dao.save(entity);
    assert.deepEqual(await dao.getAll(), [existing, entity]);

    //when
    await dao.delete(entity.id);

    //then
    const results = await dao.getAll();
    assert.deepEqual(results, [existing]);
  });

  it("should delete all records when deleteAll", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    assert.notDeepEqual(await dao.getAll(), []);

    //when
    await dao.deleteAll();

    //then
    const results = await dao.getAll();
    assert.deepEqual(results, []);
  });
});

/**
 * @param {number} id
 * @param {string} path
 * @returns {FolderShortcut}
 */
function getFolderShortcut(id, path) {
  return { id, path };
}
