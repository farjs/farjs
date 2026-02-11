/**
 * @typedef {import("@farjs/filelist/util/SubProcess.mjs").SubProcess} SubProcess
 */
import { Readable } from "stream";
import { deepEqual } from "node:assert/strict";
import mockFunction from "mock-fn";
import StreamReader from "@farjs/filelist/util/StreamReader.mjs";
import SubProcess, {
  SubProcessError,
} from "@farjs/filelist/util/SubProcess.mjs";
import ZipEntry from "../../../archiver/zip/ZipEntry.mjs";
import ZipApi from "../../../archiver/zip/ZipApi.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const { addToZip, readZip } = ZipApi;

describe("ZipApi.test.mjs", () => {
  it("should fail if exitCode != 0 when addToZip", async () => {
    //given
    const stdout = new StreamReader(Readable.from([]));
    const error = new SubProcessError(1, "test error");
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(error),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const parent = "test dir";
    const zipFile = "test.zip";
    const items = new Set(["item 1", "item 2"]);
    const onNextItem = mockFunction();

    let resError = null;
    try {
      //when
      await addToZip(zipFile, parent, items, onNextItem);
    } catch (error) {
      resError = error;
    }

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "zip",
      ["-r", "test.zip", "item 1", "item 2"],
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(onNextItem.times, 0);
    deepEqual(resError === error, true);
  });

  it("should spawn zip command when addToZip", async () => {
    //given
    const stdout = new StreamReader(
      Readable.from(
        Buffer.from(`  adding: 1/ (stored 0%)
  adding: 1/2.txt (stored 1%)
  adding: 1/1.txt (stored 2.3%)
`),
      ),
    );
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(undefined),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const parent = "test dir";
    const zipFile = "test.zip";
    const items = new Set(["item 1", "item 2"]);
    const onNextItem = mockFunction();

    //when
    await addToZip(zipFile, parent, items, onNextItem);

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "zip",
      ["-r", "test.zip", "item 1", "item 2"],
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(onNextItem.times, 3);
  });

  it("should fail if exitCode != 1 when readZip", async () => {
    //given
    const expectedOutput = `Archive:  ./1.zip
Zip file size: 22 bytes, number of entries: 0
Empty zipfile.
`;
    const stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)));
    const error = new SubProcessError(2, "test error");
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(error),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const zipPath = "/dir/filePath.zip";

    let resError = null;
    try {
      //when
      await readZip(zipPath);
    } catch (error) {
      resError = error;
    }

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "unzip",
      ["-ZT", zipPath],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(resError === error, true);
  });

  it("should fail if output doesn't contain `Empty zipfile` when readZip", async () => {
    //given
    const expectedOutput = `Archive:  ./1.zip
Zip file size: 22 bytes, number of entries: 0
`;
    const stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)));
    const error = new SubProcessError(1, "test error");
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(error),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const zipPath = "/dir/filePath.zip";

    let resError = null;
    try {
      //when
      await readZip(zipPath);
    } catch (error) {
      resError = error;
    }

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "unzip",
      ["-ZT", zipPath],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(resError === error, true);
  });

  it("should return empty map if empty zip when readZip", async () => {
    //given
    const expectedOutput = `Archive:  ./1.zip
Zip file size: 22 bytes, number of entries: 0
Empty zipfile.
`;
    const stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)));
    const error = new SubProcessError(1, "test error");
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(error),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const zipPath = "/dir/filePath.zip";

    //when
    const result = await readZip(zipPath);

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "unzip",
      ["-ZT", zipPath],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(result, new Map());
  });

  it("should spawn unzip and parse output when readZip", async () => {
    //given
    const expectedOutput = `Archive:  /test/dir/file.zip
Zip file size: 595630 bytes, number of entries: 18
-rw-r--r--  2.1 unx     1 bX defN 20190628.161923 test/dir/file.txt
18 files
`;
    const stdout = new StreamReader(Readable.from(Buffer.from(expectedOutput)));
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(undefined),
    };
    let spawnArgs = /** @type {any[]} */ ([]);
    const spawn = mockFunction((...args) => {
      spawnArgs = args;
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs = args;
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const zipPath = "/dir/filePath.zip";

    //when
    const result = await readZip(zipPath);

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "unzip",
      ["-ZT", zipPath],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(
      result,
      new Map([
        [
          "test/dir",
          [
            ZipEntry(
              "test/dir",
              "file.txt",
              false,
              1,
              dt("2019-06-28T16:19:23"),
              "-rw-r--r--",
            ),
          ],
        ],
        [
          "test",
          [
            ZipEntry(
              "test",
              "dir",
              true,
              0,
              dt("2019-06-28T16:19:23"),
              "drw-r--r--",
            ),
          ],
        ],
        [
          "",
          [
            ZipEntry(
              "",
              "test",
              true,
              0,
              dt("2019-06-28T16:19:23"),
              "drw-r--r--",
            ),
          ],
        ],
      ]),
    );
  });
});

/** @type {(s: string) => number} */
function dt(s) {
  return Date.parse(s);
}
