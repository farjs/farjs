import assert from "node:assert/strict";
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
    const result = await FarjsDBMigrations(":memory:");

    //then
    assert.deepEqual(!result, false);
  });
});
