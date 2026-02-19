/**
 * @import { FileSource } from "@farjs/filelist/api/FileListApi.mjs"
 * @typedef {import("@farjs/filelist/util/SubProcess.mjs").SubProcess} SubProcess
 */
import os from "os";
import fs from "fs";
import path from "path";
import { Readable } from "stream";
import { deepEqual } from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
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

const entriesByParentP = Promise.resolve(
  new Map([
    [
      "",
      [
        ZipEntry("", "file 1", false, 2, 3, "-rw-r--r--"),
        ZipEntry("", "dir 1", true, 0, 1, "drwxr-xr-x"),
      ],
    ],
    ["dir 1", [ZipEntry("dir 1", "dir 2", true, 0, 4, "drwxr-xr-x")]],
    [
      "dir 1/dir 2",
      [ZipEntry("dir 1/dir 2", "file 2", false, 5, 6, "-rw-r--r--")],
    ],
  ]),
);

describe("ZipApi.test.mjs", () => {
  it("should return supported capabilities", () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when & then
    deepEqual(api.isLocal, false);

    //when & then
    deepEqual(
      api.capabilities,
      new Set([FileListCapability.read, FileListCapability.delete]),
    );
  });

  it("should return root dir content when readDir('')", async () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when
    const result = await api.readDir("");

    //then
    deepEqual(result, {
      path: rootPath,
      isRoot: false,
      items: [
        ZipEntry("", "file 1", false, 2, 3, "-rw-r--r--"),
        ZipEntry("", "dir 1", true, 0, 1, "drwxr-xr-x"),
      ],
    });
  });

  it("should return root dir content when readDir('', .)", async () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when
    const result = await api.readDir("", FileListItem.currDir.name);

    //then
    deepEqual(result, {
      path: rootPath,
      isRoot: false,
      items: [
        ZipEntry("", "file 1", false, 2, 3, "-rw-r--r--"),
        ZipEntry("", "dir 1", true, 0, 1, "drwxr-xr-x"),
      ],
    });
  });

  it("should return root dir content when readDir(..)", async () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when
    const result = await api.readDir(
      `${rootPath}/dir 1/dir 2`,
      FileListItem.up.name,
    );

    //then
    deepEqual(result, {
      path: `${rootPath}/dir 1`,
      isRoot: false,
      items: [ZipEntry("dir 1", "dir 2", true, 0, 4, "drwxr-xr-x")],
    });
  });

  it("should return sub-dir content when readDir", async () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when
    const result = await api.readDir(`${rootPath}/dir 1`, "dir 2");

    //then
    deepEqual(result, {
      path: `${rootPath}/dir 1/dir 2`,
      isRoot: false,
      items: [ZipEntry("dir 1/dir 2", "file 2", false, 5, 6, "-rw-r--r--")],
    });
  });

  it("should return empty content if not existing dir when readDir", async () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);

    //when
    const result = await api.readDir(`${rootPath}/dir 1`, "dir 3");

    //then
    deepEqual(result, {
      path: `${rootPath}/dir 1/dir 3`,
      isRoot: false,
      items: [],
    });
  });

  it("should spawn zip command recursively when delete", async () => {
    //given
    const stdout = new StreamReader(
      Readable.from(
        Buffer.from(`  deleting 1
  deleting 2
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
      spawnArgs.push(args);
      return /** @type {any} */ (subProcess.child);
    });
    let wrapArgs = /** @type {any[]} */ ([]);
    const wrap = mockFunction((...args) => {
      wrapArgs.push(args);
      return Promise.resolve(subProcess);
    });
    SubProcess.spawn = spawn;
    SubProcess.wrap = wrap;
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(
      zipPath,
      rootPath,
      Promise.resolve(
        new Map([
          ["", [ZipEntry("", "dir 1", true, 0, 1, "drwxr-xr-x")]],
          [
            "dir 1",
            [
              ZipEntry("dir 1", "file 1", false, 2, 3, "-rw-r--r--"),
              ZipEntry("dir 1", "dir 2", true, 0, 4, "drwxr-xr-x"),
            ],
          ],
          [
            "dir 1/dir 2",
            [
              ZipEntry("dir 1/dir 2", "file 2", false, 5, 6, "-rw-r--r--"),
              ZipEntry("dir 1/dir 2", "dir 3", true, 0, 7, "drwxr-xr-x"),
            ],
          ],
        ]),
      ),
    );
    const parent = rootPath;
    const items = [FileListItem("dir 1", true)];

    //when
    await api.delete(parent, items);
    const result = await api.readDir(parent);

    //then
    deepEqual(result.items, []);
    deepEqual(spawn.times, 3);
    deepEqual(spawnArgs, [
      [
        "zip",
        ["-qd", zipPath, "dir 1/dir 2/file 2", "dir 1/dir 2/dir 3/"],
        {
          windowsHide: true,
        },
      ],
      [
        "zip",
        ["-qd", zipPath, "dir 1/file 1", "dir 1/dir 2/"],
        {
          windowsHide: true,
        },
      ],
      [
        "zip",
        ["-qd", zipPath, "dir 1/"],
        {
          windowsHide: true,
        },
      ],
    ]);
    deepEqual(wrap.times, 3);
    deepEqual(wrapArgs, [
      [subProcess.child],
      [subProcess.child],
      [subProcess.child],
    ]);
  });

  it("should fail if exitError when delete", async () => {
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
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);
    const parent = rootPath;
    const items = [FileListItem("dir 1", true)];

    let resError = null;
    try {
      //when
      await api.delete(parent, items);
    } catch (error) {
      resError = error;
    }

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "zip",
      ["-qd", zipPath, "dir 1/dir 2/file 2"],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);
    deepEqual(resError === error, true);
  });

  it("should read content fully when readFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    const file = path.join(tmpDir, "example.txt");
    const expectedOutput = "hello, World!!!";
    fs.writeFileSync(file, expectedOutput);
    const stdoutStream = fs.createReadStream(file);
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout: new StreamReader(stdoutStream),
      exitP: Promise.resolve(undefined),
    };
    SubProcess.spawn = mockFunction();
    SubProcess.wrap = mockFunction();
    let extractArgs = /** @type {any[]} */ ([]);
    const extract = mockFunction((...args) => {
      extractArgs = args;
      return Promise.resolve(subProcess);
    });
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    class TestZipApi extends ZipApi {
      constructor() {
        super(zipPath, rootPath, entriesByParentP);
        this.extract = extract;
      }
    }
    const api = new TestZipApi();
    const item = {
      ...FileListItem("example.txt"),
      size: expectedOutput.length,
    };

    /** @type {(source: FileSource, chunks?: Buffer[]) => Promise<string>} */
    async function loop(source, chunks = []) {
      const buff = Buffer.alloc(5);
      const bytesRead = await source.readNextBytes(buff);
      if (bytesRead > 0) {
        chunks.push(buff.subarray(0, bytesRead));
        return loop(source, chunks);
      }

      return Buffer.concat(chunks).toString();
    }

    //when
    const source = await api.readFile(`${rootPath}/dir 1`, item, 0);
    const output = await loop(source);
    await source.close();

    //then
    const expectedFilePath = "dir 1/example.txt";
    deepEqual(source.file, expectedFilePath);
    deepEqual(output, expectedOutput);

    deepEqual(extract.times, 1);
    deepEqual(extractArgs, ["/dir/filePath.zip", expectedFilePath]);

    //cleanup
    fs.unlinkSync(file);
    deepEqual(fs.existsSync(file), false);

    fs.rmdirSync(tmpDir);
    deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should destroy stream on close if partially read when readFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    const file = path.join(tmpDir, "example.txt");
    const expectedOutput = "hello";
    fs.writeFileSync(file, expectedOutput);
    const stdoutStream = fs.createReadStream(file, {
      highWaterMark: 5,
    });
    const stdout = new StreamReader(stdoutStream);
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(undefined),
    };
    SubProcess.spawn = mockFunction();
    SubProcess.wrap = mockFunction();
    let extractArgs = /** @type {any[]} */ ([]);
    const extract = mockFunction((...args) => {
      extractArgs = args;
      return Promise.resolve(subProcess);
    });
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    class TestZipApi extends ZipApi {
      constructor() {
        super(zipPath, rootPath, entriesByParentP);
        this.extract = extract;
      }
    }
    const api = new TestZipApi();
    const item = {
      ...FileListItem("example.txt"),
      size: expectedOutput.length * 2,
    };
    const buff = Buffer.alloc(5);

    //when
    const source = await api.readFile(`${rootPath}/dir 1`, item, 0);
    //when & then
    deepEqual(await source.readNextBytes(buff), expectedOutput.length);
    deepEqual(await source.readNextBytes(buff), 0);
    await source.close();

    //then
    const expectedFilePath = "dir 1/example.txt";
    deepEqual(source.file, expectedFilePath);
    deepEqual(await stdout.readNextBytes(5), undefined);

    deepEqual(extract.times, 1);
    deepEqual(extractArgs, ["/dir/filePath.zip", expectedFilePath]);

    //cleanup
    fs.unlinkSync(file);
    deepEqual(fs.existsSync(file), false);

    fs.rmdirSync(tmpDir);
    deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should fail on readNextBytes if error when readFile", async () => {
    //given
    const stdout = new StreamReader(Readable.from([]));
    const error = new SubProcessError(1, "test error");
    /** @type {SubProcess} */
    const subProcess = {
      child: /** @type {any} */ ({}),
      stdout,
      exitP: Promise.resolve(error),
    };
    SubProcess.spawn = mockFunction();
    SubProcess.wrap = mockFunction();
    let extractArgs = /** @type {any[]} */ ([]);
    const extract = mockFunction((...args) => {
      extractArgs = args;
      return Promise.resolve(subProcess);
    });
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    class TestZipApi extends ZipApi {
      constructor() {
        super(zipPath, rootPath, entriesByParentP);
        this.extract = extract;
      }
    }
    const api = new TestZipApi();
    const item = {
      ...FileListItem("example.txt"),
      size: 5,
    };
    const source = await api.readFile(`${rootPath}/dir 1`, item, 0);
    const buff = Buffer.alloc(5);

    let resError = null;
    try {
      //when
      await source.readNextBytes(buff);
    } catch (error) {
      resError = error;
    }

    //when & then
    await source.close();

    //then
    deepEqual(resError === error, true);
    const expectedFilePath = "dir 1/example.txt";
    deepEqual(source.file, expectedFilePath);

    deepEqual(extract.times, 1);
    deepEqual(extractArgs, ["/dir/filePath.zip", expectedFilePath]);
  });

  it("should spawn unzip command when extract", async () => {
    //given
    const expectedOutput = "hello, World!!!";
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
    const rootPath = "zip://filePath.zip";
    const api = new ZipApi(zipPath, rootPath, entriesByParentP);
    const filePath = `${rootPath}/dir 1/file 2.txt`;

    //when
    const result = await api.extract(zipPath, filePath);

    //then
    deepEqual(spawn.times, 1);
    deepEqual(spawnArgs, [
      "unzip",
      ["-p", zipPath, filePath],
      {
        windowsHide: true,
      },
    ]);
    deepEqual(wrap.times, 1);
    deepEqual(wrapArgs, [subProcess.child]);

    /** @type {(reader: StreamReader, result: string) => Promise<string>} */
    async function loop(reader, result) {
      const content = await reader.readNextBytes(5);
      if (content) {
        return loop(reader, result + content.toString());
      }

      return result;
    }
    deepEqual(await loop(result.stdout, ""), expectedOutput);
  });

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

  it("should return new ZipApi when create", () => {
    //given
    const zipPath = "/dir/filePath.zip";
    const rootPath = "zip://filePath.zip";
    const entriesByParentP = Promise.resolve(new Map());

    //when
    const result = ZipApi.create(zipPath, rootPath, entriesByParentP);

    //then
    deepEqual(result.zipPath, zipPath);
    deepEqual(result.rootPath, rootPath);
  });
});

/** @type {(s: string) => number} */
function dt(s) {
  return Date.parse(s);
}
