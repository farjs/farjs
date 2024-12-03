/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryKind} HistoryKind
 * @typedef {import("../../../dao/HistoryKindDao.mjs").HistoryKindDao} HistoryKindDao
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import testDb from "../../db.mjs";
import HistoryDao from "../../../dao/HistoryDao.mjs";
import HistoryKindDao from "../../../dao/HistoryKindDao.mjs";
import HistoryProviderImpl from "../../../app/service/HistoryProviderImpl.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const maxItemsCount = 10;

describe("HistoryProviderImpl.test.mjs", () => {
  it("should save and read history", async () => {
    //given
    const db = await testDb();
    const kindDao = HistoryKindDao(db);
    const provider = HistoryProviderImpl(db, kindDao);

    const dao0 = HistoryDao(
      db,
      { id: -1, name: "non-existing" },
      maxItemsCount
    );
    const entity = /** @type {History} */ ({
      item: "test/path",
      params: undefined,
    });
    await dao0.deleteAll();
    await kindDao.deleteAll();

    //when
    const service1 = await provider.get({ name: "test_kind", maxItemsCount });
    const service2 = await provider.get({ name: "test_kind", maxItemsCount });
    await service2.save(entity);

    //then
    const results = await service2.getAll();
    const result = await service2.getOne(entity.item);
    assert.deepEqual(service1 == service2, true);
    assert.deepEqual(results, [entity]);
    assert.deepEqual(result, entity);
  });

  it("should return noop service and log error when get", async () => {
    //given
    const savedConsoleError = console.error;
    let capturedError = "";
    const errorLogger = mockFunction((error) => {
      capturedError = error;
    });
    console.error = errorLogger;
    const daoMock = {
      ...HistoryKindDaoMock(),
      upsert: () => Promise.reject(Error("test error")),
    };
    const db = await testDb();
    const provider = HistoryProviderImpl(db, daoMock);
    const kind = /** @type {HistoryKind} */ ({
      name: "test_kind",
      maxItemsCount,
    });
    const entity = /** @type {History} */ ({
      item: "test/path",
      params: undefined,
    });

    //when & then
    const noopService = await provider.get(kind);
    await noopService.save(entity);
    const results = await noopService.getAll();
    const result = await noopService.getOne(entity.item);

    //then
    console.error = savedConsoleError;
    assert.deepEqual(errorLogger.times, 1);
    assert.deepEqual(
      capturedError,
      `Failed to upsert history kind '${kind.name}', error: Error: test error`
    );
    assert.deepEqual(results, []);
    assert.deepEqual(result, undefined);
  });

  it("should limit maxItemsCount from 5 to 150 when _limitMaxItemsCount", () => {
    //when & then
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(-1), 5);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(0), 5);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(1), 5);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(5), 5);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(6), 6);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(150), 150);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(151), 150);
    assert.deepEqual(HistoryProviderImpl._limitMaxItemsCount(152), 150);
  });

  // it("should recover and log error when save", async () => {
  //   //given
  //   const savedConsoleError = console.error;
  //   let capturedError = "";
  //   const errorLogger = mockFunction((error) => {
  //     capturedError = error;
  //   });
  //   console.error = errorLogger;
  //   const daoMock = {
  //     ...HistoryDaoMock(),
  //     save: () => Promise.reject(Error("test error")),
  //   };
  //   const service = HistoryServiceImpl(daoMock);

  //   //when
  //   await service.save({ item: "test item" });

  //   //then
  //   console.error = savedConsoleError;
  //   assert.deepEqual(errorLogger.times, 1);
  //   assert.deepEqual(
  //     capturedError,
  //     "Failed to save history item, error: Error: test error"
  //   );
  // });
});

/**
 * @returns {HistoryKindDao}
 */
function HistoryKindDaoMock() {
  return {
    getAll: () => Promise.reject(Error("test stub")),
    upsert: () => Promise.reject(Error("test stub")),
    deleteAll: () => Promise.reject(Error("test stub")),
  };
}
