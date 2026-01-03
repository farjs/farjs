import os from "os";
import path from "path";
import assert from "node:assert/strict";
import FarjsData from "../../app/FarjsData.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FarjsData.test.mjs", () => {
  it("should return DB file path on Mac OS", () => {
    //when & then
    assert.deepEqual(
      FarjsData("darwin").getDBFilePath(),
      path.join(
        os.homedir(),
        "Library",
        "Application Support",
        "FAR.js",
        "farjs.db"
      )
    );
  });

  it("should return DB file path on Windows when APPDATA is set", () => {
    //given
    const data = FarjsData("win32");
    const dataDir = "test";
    process.env.APPDATA = dataDir;

    //when & then
    assert.deepEqual(
      data.getDBFilePath(),
      path.join(dataDir, "FAR.js", "farjs.db")
    );
  });

  it("should return DB file path on Windows when APPDATA is not set", () => {
    //given
    const data = FarjsData("win32");
    delete process.env.APPDATA;
    assert.deepEqual(process.env.APPDATA === undefined, true);

    //when & then
    assert.deepEqual(
      data.getDBFilePath(),
      path.join(os.homedir(), ".FAR.js", "farjs.db")
    );
  });

  it("should return DB file path on Linux", () => {
    //given
    const data = FarjsData("linux");

    //when & then
    assert.deepEqual(
      data.getDBFilePath(),
      path.join(os.homedir(), ".local", "share", "FAR.js", "farjs.db")
    );
  });
});
