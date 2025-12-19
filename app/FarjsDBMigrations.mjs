import { readBundle, runBundle } from "@farjs/better-sqlite3-migrate";
import BetterSqlite3Database from "@farjs/better-sqlite3-wrapper";

/**
 * @param {string} dbName
 * @returns {Promise<BetterSqlite3Database.Database>}
 */
async function FarjsDBMigrations(dbName) {
  const db = new BetterSqlite3Database(dbName);
  const module = "../dao/migrations/bundle.json";
  const url = new URL(module, import.meta.url);

  const bundle = await readBundle(url);
  await runBundle(db, bundle);
  return db;
}

export default FarjsDBMigrations;
