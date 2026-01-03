/**
 * @import { Database } from "@farjs/better-sqlite3-wrapper"
 * @import FileListActions from "@farjs/filelist/FileListActions.mjs"
 * @import { FarjsData } from "./FarjsData.mjs"
 */
import TaskManager from "@farjs/ui/task/TaskManager.mjs";
import { readBundle, runBundle } from "@farjs/better-sqlite3-migrate";
import BetterSqlite3Database from "@farjs/better-sqlite3-wrapper";

/**
 * @param {string} dbName
 * @returns {Promise<Database>}
 */
async function FarjsDBMigrations(dbName) {
  const db = new BetterSqlite3Database(dbName);
  const module = "../dao/migrations/bundle.json";
  const url = new URL(module, import.meta.url);

  const bundle = await readBundle(url);
  await runBundle(db, bundle);
  return db;
}

/** @type {(actions: FileListActions, appData: FarjsData) => Promise<Database>} */
FarjsDBMigrations.prepareDB = (actions, appData) => {
  const dbP = actions.api.mkDirs(appData.getDataDir()).then(() => {
    return FarjsDBMigrations(appData.getDBFilePath());
  });

  return dbP.catch((error) => {
    console.error(`Failed to prepare DB, error: ${error}`);
    TaskManager.errorHandler(error);
    return dbP;
  });
};

export default FarjsDBMigrations;
