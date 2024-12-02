/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("../../../dao/HistoryDao.mjs").HistoryDao} HistoryDao
 * @typedef {import("../../../dao/HistoryKindDao.mjs").HistoryKindEntity} HistoryKindEntity
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import testDb from "../../db.mjs";
import HistoryDao from "../../../dao/HistoryDao.mjs";
import HistoryKindDao from "../../../dao/HistoryKindDao.mjs";
import HistoryServiceImpl from "../../../app/service/HistoryServiceImpl.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const params = {
  isEdit: false,
  encoding: "test-encoding",
  position: 123,
  wrap: true,
  column: 4,
};

describe("HistoryServiceImpl.test.mjs", () => {
  it("should store and read items", async () => {
    //given
    const db = await testDb();
    const kindDao = HistoryKindDao(db);
    const maxItemsCount = 10;
    const dao0 = HistoryDao(
      db,
      { id: -1, name: "non-existing" },
      maxItemsCount
    );
    await dao0.deleteAll();
    await kindDao.deleteAll();
    const kind = await kindDao.upsert({ id: -1, name: "test_kind1" });
    const service = HistoryServiceImpl(HistoryDao(db, kind, maxItemsCount));

    //when
    const entity1 = /** @type {History} */ ({
      item: "test/path/1",
      params: undefined,
    });
    const entity2 = /** @type {History} */ ({ item: "test/path/2", params });
    await service.save(entity1);
    await service.save(entity2);

    //then
    const results = await service.getAll();
    const result1 = await service.getOne(entity1.item);
    const result2 = await service.getOne(entity2.item);
    assert.deepEqual(result1, entity1);
    assert.deepEqual(result2, entity2);
    assert.deepEqual(results, [entity1, entity2]);
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
      ...HistoryDaoMock(),
      getAll: () => Promise.reject(Error("test error")),
    };
    const service = HistoryServiceImpl(daoMock);

    //when
    const results = await service.getAll();

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to read all history items, error: Error: test error"
    );
    assert.deepEqual(results, []);
  });

  it("should recover and log error when getOne", async () => {
    //given
    const savedConsoleError = console.error;
    let capturedError = "";
    const errorLogger = mockFunction((error) => {
      capturedError = error;
    });
    console.error = errorLogger;
    const daoMock = {
      ...HistoryDaoMock(),
      getByItem: () => Promise.reject(Error("test error")),
    };
    const service = HistoryServiceImpl(daoMock);

    //when
    const result = await service.getOne("test item");

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to read history item, error: Error: test error"
    );
    assert.deepEqual(result, undefined);
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
      ...HistoryDaoMock(),
      save: () => Promise.reject(Error("test error")),
    };
    const service = HistoryServiceImpl(daoMock);

    //when
    await service.save({ item: "test item" });

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      "Failed to save history item, error: Error: test error"
    );
  });
});

/**
 * @returns {HistoryDao}
 */
function HistoryDaoMock() {
  return {
    getAll: () => Promise.reject(Error("test stub")),
    getByItem: () => Promise.reject(Error("test stub")),
    save: () => Promise.reject(Error("test stub")),
    deleteAll: () => Promise.reject(Error("test stub")),
  };
}
