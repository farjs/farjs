import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import testDb from "../db.mjs";
import FarjsDBMigrations from "../../app/FarjsDBMigrations.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FarjsDBMigrations.test.mjs", () => {
  it("should successfully apply db migrations in memory db", async () => {
    //when
    const result = await testDb();

    //then
    assert.deepEqual(!result, false);
  });

  it("should log error if failed", async () => {
    //given
    let errorArgs = /** @type {any[]} */ ([]);
    const errorMock = mockFunction((...args) => {
      errorArgs.push(...args);
    });
    const savedError = console.error;
    console.error = errorMock;
    const error = Error("test error");
    const mkDirs = mockFunction(() => {
      return Promise.reject(error);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ mkDirs }),
    });

    //when
    let resError = null;
    try {
      await FarjsDBMigrations.prepareDB(actions, {
        getDataDir: mockFunction(),
        getDBFilePath: mockFunction(),
      });
    } catch (error) {
      resError = error;
    }

    //then
    console.error = savedError;
    assert.deepEqual(resError === error, true);
    assert.deepEqual(errorMock.times, 2);
    assert.deepEqual(errorArgs[0], `Failed to prepare DB, error: ${error}`);
    assert.deepEqual(errorArgs[1].startsWith(`${error}\n    at`), true);
  });
});
