import Database from "@farjs/better-sqlite3-wrapper";
import { readBundle, runBundle } from "@farjs/better-sqlite3-migrate";

/** @type {Database.Database | undefined} */
let _testDb = undefined;

async function testDb() {
  if (!_testDb) {
    _testDb = new Database(":memory:");
    const bundleUrl = new URL("../dao/migrations/bundle.json", import.meta.url);
    const bundle = await readBundle(bundleUrl);
    await runBundle(_testDb, bundle);
  }

  return _testDb;
}

export default testDb;
