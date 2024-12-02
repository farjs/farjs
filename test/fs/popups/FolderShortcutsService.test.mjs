/**
 * @typedef {import("../../../dao/FolderShortcutDao.mjs").FolderShortcutDao} FolderShortcutDao
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import testDb from "../../db.mjs";
import FolderShortcutDao from "../../../dao/FolderShortcutDao.mjs";
import FolderShortcutsService from "../../../fs/popups/FolderShortcutsService.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FolderShortcutsService.test.mjs", () => {
  it("should store new folder shortcut when save", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    const service = FolderShortcutsService(dao);
    const path = "test/path";
    const expected = new Array(10).fill(undefined);
    await dao.deleteAll();
    assert.deepEqual(await service.getAll(), expected);

    //when
    await service.save(1, path);

    //then
    const results = await service.getAll();
    expected[1] = path;
    assert.deepEqual(results, expected);
  });

  it("should update existing folder shortcut when save", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    const service = FolderShortcutsService(dao);
    const path = "test/path";
    await dao.deleteAll();
    await service.save(1, path);
    const expected = new Array(10).fill(undefined);
    expected[1] = path;
    assert.deepEqual(await service.getAll(), expected);

    //when
    await service.save(1, `${path}-updated`);

    //then
    const results = await service.getAll();
    expected[1] = `${path}-updated`;
    assert.deepEqual(results, expected);
  });

  it("should delete folder shortcut when delete", async () => {
    //given
    const db = await testDb();
    const dao = FolderShortcutDao(db);
    const service = FolderShortcutsService(dao);
    const path = "test/path";
    await dao.deleteAll();
    await service.save(1, path);
    const expected = new Array(10).fill(undefined);
    expected[1] = path;
    assert.deepEqual(await service.getAll(), expected);

    //when
    await service.delete(1);

    //then
    const results = await service.getAll();
    expected[1] = undefined;
    assert.deepEqual(results, expected);
  });

  it("should recover and log error when getAll", async () => {
    //given
    const savedConsoleError = console.error;
    let capturedError = "";
    const errorLogger = mockFunction((error) => {
      capturedError = error;
    });
    console.error = errorLogger;
    const daoMock = {
      ...FolderShortcutDaoMock(),
      getAll: () => Promise.reject(Error("test error")),
    };
    const service = FolderShortcutsService(daoMock);

    //when
    const results = await service.getAll();

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to read folder shortcuts, error: Error: test error"
    );
    assert.deepEqual(results, []);
  });

  it("should recover and log error when save", async () => {
    //given
    const savedConsoleError = console.error;
    let capturedError = "";
    const errorLogger = mockFunction((error) => {
      capturedError = error;
    });
    console.error = errorLogger;
    const daoMock = {
      ...FolderShortcutDaoMock(),
      save: () => Promise.reject(Error("test error")),
    };
    const service = FolderShortcutsService(daoMock);

    //when
    await service.save(1, "test");

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to save folder shortcut, error: Error: test error"
    );
  });

  it("should recover and log error when delete", async () => {
    //given
    const savedConsoleError = console.error;
    let capturedError = "";
    const errorLogger = mockFunction((error) => {
      capturedError = error;
    });
    console.error = errorLogger;
    const daoMock = {
      ...FolderShortcutDaoMock(),
      delete: () => Promise.reject(Error("test error")),
    };
    const service = FolderShortcutsService(daoMock);

    //when
    await service.delete(1);

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to delete folder shortcut, error: Error: test error"
    );
  });
});

/**
 * @returns {FolderShortcutDao}
 */
function FolderShortcutDaoMock() {
  return {
    getAll: () => Promise.reject(Error("test stub")),
    save: () => Promise.reject(Error("test stub")),
    delete: () => Promise.reject(Error("test stub")),
    deleteAll: () => Promise.reject(Error("test stub")),
  };
}
