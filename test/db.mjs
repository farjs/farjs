/**
 * @import { Database } from "@farjs/better-sqlite3-wrapper"
 */
import { lazyFn } from "@farjs/filelist/utils.mjs";
import FarjsDBMigrations from "../app/FarjsDBMigrations.mjs";

/** @type {() => Promise<Database>} */
const testDb = lazyFn(() => FarjsDBMigrations(":memory:"));

export default testDb;
