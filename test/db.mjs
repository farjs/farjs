/**
 * @import { Database } from "@farjs/better-sqlite3-wrapper"
 */
import mockFunction from "mock-fn";
import { lazyFn } from "@farjs/filelist/utils.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import FarjsDBMigrations from "../app/FarjsDBMigrations.mjs";

/** @type {() => Promise<Database>} */
const testDb = lazyFn(() => {
  const mkDirs = mockFunction(() => {
    return Promise.resolve("test/db_path");
  });
  const getDataDir = mockFunction(() => {
    return ["test", "db_path"];
  });
  const getDBFilePath = mockFunction(() => {
    return ":memory:";
  });
  const actions = new MockFileListActions({
    api: new MockFileListApi({ mkDirs }),
  });

  return FarjsDBMigrations.prepareDB(actions, { getDataDir, getDBFilePath });
});

export default testDb;
