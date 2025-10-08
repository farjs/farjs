/**
 * @import { FileSource, FileTarget } from "@farjs/filelist/api/FileListApi.mjs"
 * @import { FSDisk } from "../../fs/FSDisk.mjs"
 */
import path from "path";
import fs from "fs";
import os from "os";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import { stripPrefix } from "@farjs/filelist/utils.mjs";
import MockFSService from "../../fs/MockFSService.mjs";
import FSFileListApi from "../../fs/FSFileListApi.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const { _toFileListItem } = FSFileListApi;

const apiImp = new FSFileListApi();
const FSConstants = fs.constants;
const fsMocks = {
  lstatSync: mockFunction(),
  rmdirSync: mockFunction(),
  unlinkSync: mockFunction(),
  mkdir: mockFunction(),
  readdir: mockFunction(),
  open: mockFunction(),
};

describe("FSFileListApi.test.mjs", () => {
  it("should return supported capabilities", () => {
    //when & then
    assert.deepEqual(apiImp.isLocal, true);
    assert.deepEqual(
      apiImp.capabilities,
      new Set([
        FileListCapability.read,
        FileListCapability.write,
        FileListCapability.delete,
        FileListCapability.mkDirs,
        FileListCapability.copyInplace,
        FileListCapability.moveInplace,
      ])
    );
  });

  it("should not fail if fs.lstatSync fails when readDir", async () => {
    //given
    let readdirArgs = /** @type {any[]} */ ([]);
    const readdir = mockFunction((...args) => {
      readdirArgs = args;
      return readdirP;
    });
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    const lstatSync = mockFunction((...args) => {
      lstatSyncArgs.push(args);
      throw Error("test error");
    });
    const readdirP = Promise.resolve(["file1", "file2"]);
    const apiImp = new FSFileListApi({
      ...fsMocks,
      lstatSync,
      readdir,
    });
    const targetDir = path.resolve(FileListItem.currDir.name);

    //when
    const result = await apiImp.readDir(targetDir, undefined);

    //then
    assert.deepEqual(readdir.times, 1);
    assert.deepEqual(readdirArgs, [targetDir]);
    assert.deepEqual(lstatSync.times, 2);
    assert.deepEqual(lstatSyncArgs, [
      [path.join(targetDir, "file1")],
      [path.join(targetDir, "file2")],
    ]);
    assert.deepEqual(result, {
      path: process.cwd(),
      isRoot: false,
      items: [FileListItem("file1"), FileListItem("file2")],
    });
  });

  it("should return current dir info and files when readDir('', .)", async () => {
    //when
    const result = await apiImp.readDir("", FileListItem.currDir.name);

    //then
    assert.deepEqual(result.items.length > 0, true);
    assert.deepEqual(result, {
      path: process.cwd(),
      isRoot: false,
      items: result.items,
    });
  });

  it("should return parent dir info and files when readDir(dir, ..)", async () => {
    //given
    const curr = process.cwd();
    const currDirObj = path.parse(curr);
    const parentDir = currDirObj.dir ?? "";
    const currDir = currDirObj.base ?? "";
    assert.deepEqual(parentDir.length > 0, true);
    assert.deepEqual(currDir.length > 0, true);

    //when
    const result = await apiImp.readDir(curr, FileListItem.up.name);

    //then
    assert.deepEqual(result.items.length > 0, true);
    assert.deepEqual(result, {
      path: parentDir,
      isRoot: false,
      items: result.items,
    });
  });

  it("should return target dir info and files when readDir(dir, sub-dir)", async () => {
    //given
    const curr = process.cwd();
    const currDirObj = path.parse(curr);
    const parentDir = currDirObj.dir ?? "";
    const subDir = currDirObj.base ?? "";
    assert.deepEqual(parentDir.length > 0, true);
    assert.deepEqual(subDir.length > 0, true);

    //when
    const result = await apiImp.readDir(parentDir, subDir);

    //then
    assert.deepEqual(result.items.length > 0, true);
    assert.deepEqual(result, {
      path: curr,
      isRoot: false,
      items: result.items,
    });
  });

  it("should return root dir info and files when readDir(root, .)", async () => {
    //given
    const curr = process.cwd();
    const currDirObj = path.parse(curr);
    const currRoot = currDirObj.root ?? "";
    assert.deepEqual(currRoot.length > 0, true);

    //when
    const result = await apiImp.readDir(currRoot, FileListItem.currDir.name);

    //then
    assert.deepEqual(result.items.length > 0, true);
    assert.deepEqual(result, {
      path: currRoot,
      isRoot: true,
      items: result.items,
    });
  });

  it("should delete items when delete", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);

    /** @type {(parent: string, name: string, isDir: boolean) => [string, string]} */
    function create(parent, name, isDir) {
      const fullPath = path.join(parent, name);
      if (isDir) fs.mkdirSync(fullPath);
      else fs.writeFileSync(fullPath, `file: ${fullPath}`);

      return [fullPath, name];
    }

    const [d1, d1Name] = create(tmpDir, "dir1", true);
    const [f1, f1Name] = create(tmpDir, "file1.txt", false);
    const [d2] = create(d1, "dir2", true);
    const [f2] = create(d1, "file2.txt", false);
    const [d3] = create(d2, "dir3", true);
    const [f3] = create(d2, "file3.txt", false);
    const items = [FileListItem(d1Name, true), FileListItem(f1Name)];

    //when
    await apiImp.delete(tmpDir, items);

    //then
    assert.deepEqual(fs.existsSync(d1), false);
    assert.deepEqual(fs.existsSync(f1), false);
    assert.deepEqual(fs.existsSync(d2), false);
    assert.deepEqual(fs.existsSync(f2), false);
    assert.deepEqual(fs.existsSync(d3), false);
    assert.deepEqual(fs.existsSync(f3), false);

    //cleanup
    del(f3, false);
    del(f2, false);
    del(f1, false);
    del(d3, true);
    del(d2, true);
    del(d1, true);
    del(tmpDir, true);
  });

  it("should create multiple directories when mkDirs", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const dirs = ["test1", "test2", "", "test3", ""];
    const resPath = path.join(tmpDir, ...dirs);

    //when
    const result = await apiImp.mkDirs([tmpDir, ...dirs]);

    //then
    assert.deepEqual(result, resPath);
    assert.deepEqual(fs.existsSync(resPath), true);

    //cleanup
    del(resPath, true);
    del(path.join(tmpDir, "test1", "test2"), true);
    del(path.join(tmpDir, "test1"), true);
    del(tmpDir, true);
  });

  it("should not fail if dir already exists when mkDirs", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const topDir = "test1";
    const dirs = [topDir, "test2", "", "test3", ""];
    const resPath = path.join(tmpDir, ...dirs);
    fs.mkdirSync(path.join(tmpDir, topDir));
    assert.deepEqual(fs.existsSync(path.join(tmpDir, topDir)), true);

    //when
    const result = await apiImp.mkDirs([tmpDir, ...dirs]);

    //then
    assert.deepEqual(result, resPath);
    assert.deepEqual(fs.existsSync(resPath), true);

    //cleanup
    del(resPath, true);
    del(path.join(tmpDir, "test1", "test2"), true);
    del(path.join(tmpDir, "test1"), true);
    del(tmpDir, true);
  });

  it("should fail if other error when mkDirs", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const dir = "test123";
    const resPath = path.join(tmpDir, dir);
    const error = Error("test error");

    let mkdirArgs = /** @type {any[]} */ ([]);
    const mkdir = mockFunction((...args) => {
      mkdirArgs = args;
      return Promise.reject(error);
    });
    const apiImp = new FSFileListApi({
      ...fsMocks,
      mkdir,
    });

    //when
    let resError = null;
    try {
      await apiImp.mkDirs([resPath]);
    } catch (error) {
      resError = error;
    }

    //then
    assert.deepEqual(resError === error, true);
    assert.deepEqual(mkdir.times, 1);
    assert.deepEqual(mkdirArgs, [resPath]);

    //cleanup
    del(resPath, true);
    del(tmpDir, true);
  });

  it("should create single directory when mkDirs", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const dir = "test123";
    const resPath = path.join(tmpDir, dir);

    //when
    const result = await apiImp.mkDirs([resPath]);

    //then
    assert.deepEqual(result, resPath);
    assert.deepEqual(fs.existsSync(resPath), true);

    //cleanup
    del(resPath, true);
    del(tmpDir, true);
  });

  it("should skip root directory creation when mkDirs", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const tmpDirObj = path.parse(tmpDir);
    const tmpRoot = tmpDirObj.root ?? "";
    assert.deepEqual(tmpRoot.length > 0, true);
    const tmpRest = stripPrefix(stripPrefix(tmpDir, tmpRoot), path.sep);
    const dir = "skip_root_dir_creation_test";
    const resPath = path.join(tmpRoot, tmpRest, dir);
    assert.deepEqual(fs.existsSync(resPath), false);

    //when
    const result = await apiImp.mkDirs([tmpRoot, tmpRest, dir]);

    //then
    assert.deepEqual(result, resPath);
    assert.deepEqual(fs.existsSync(resPath), true);

    //cleanup
    del(resPath, true);
    del(tmpDir, true);
    assert.deepEqual(fs.existsSync(resPath), false);
  });

  it("should copy new file when readFile/writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);

    const file1 = path.join(tmpDir, "example.txt");
    const file2 = path.join(tmpDir, "example2.txt");
    fs.writeFileSync(file1, "hello, World!!!");
    assert.deepEqual(fs.existsSync(file1), true);

    const onExists = mockFunction();
    const stats1 = fs.lstatSync(file1);
    const buff = new Uint8Array(5);

    /** @type {(source: FileSource, target: FileTarget) => Promise<void>} */
    async function loop(source, target) {
      const bytesRead = await source.readNextBytes(buff);
      if (bytesRead === 0) {
        return target.setAttributes(_toFileListItem("example.txt", stats1));
      }
      return target
        .writeNextBytes(buff, bytesRead)
        .then((_) => loop(source, target));
    }

    //when
    const source = await apiImp.readFile(
      tmpDir,
      FileListItem("example.txt"),
      0.0
    );
    assert.deepEqual(source.file, file1);
    const target = await apiImp.writeFile(tmpDir, "example2.txt", onExists);
    if (!target) {
      assert.fail("target is undefined!");
    }
    await loop(source, target);
    await target.close();
    await source.close();

    //then
    assert.deepEqual(onExists.times, 0);
    const stats2 = fs.lstatSync(file2);
    assert.deepEqual(stats2.size, stats1.size);
    assert.deepEqual(
      toDateTimeStr(stats2.atimeMs),
      toDateTimeStr(stats1.atimeMs)
    );
    assert.deepEqual(
      toDateTimeStr(stats2.mtimeMs),
      toDateTimeStr(stats1.mtimeMs)
    );
    assert.deepEqual(
      fs.readFileSync(file2, {
        encoding: "utf8",
      }),
      "hello, World!!!"
    );

    //cleanup
    fs.unlinkSync(file1);
    fs.unlinkSync(file2);
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should overwrite existing file when readFile/writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);

    const file1 = path.join(tmpDir, "example.txt");
    const file2 = path.join(tmpDir, "example2.txt");
    fs.writeFileSync(file1, "hello, World");
    fs.writeFileSync(file2, "hello, World!!!");
    assert.deepEqual(fs.existsSync(file1), true);
    assert.deepEqual(fs.existsSync(file2), true);

    let onExistsArgs = /** @type {any[]} */ ([]);
    const onExists = mockFunction((...args) => {
      onExistsArgs = args;
      return Promise.resolve(true);
    });
    const srcItem = _toFileListItem("example.txt", fs.lstatSync(file1));
    const existing = _toFileListItem("example2.txt", fs.lstatSync(file2));
    const buff = new Uint8Array(5);

    /** @type {(source: FileSource, target: FileTarget) => Promise<void>} */
    async function loop(source, target) {
      const bytesRead = await source.readNextBytes(buff);
      if (bytesRead === 0) {
        return target.setAttributes(srcItem);
      }
      return target
        .writeNextBytes(buff, bytesRead)
        .then((_) => loop(source, target));
    }

    //when
    const source = await apiImp.readFile(
      tmpDir,
      FileListItem("example.txt"),
      0.0
    );
    const target = await apiImp.writeFile(tmpDir, "example2.txt", onExists);
    if (!target) {
      assert.fail("target is undefined!");
    }
    await loop(source, target);
    await target.close();
    await source.close();

    //then
    assert.deepEqual(onExists.times, 1);
    assert.deepEqual(onExistsArgs, [existing]);
    const stats2 = fs.lstatSync(file2);
    assert.deepEqual(stats2.size, srcItem.size);
    assert.deepEqual(
      toDateTimeStr(stats2.atimeMs),
      toDateTimeStr(srcItem.atimeMs)
    );
    assert.deepEqual(
      toDateTimeStr(stats2.mtimeMs),
      toDateTimeStr(srcItem.mtimeMs)
    );
    assert.deepEqual(
      fs.readFileSync(file2, {
        encoding: "utf8",
      }),
      "hello, World"
    );

    //cleanup
    fs.unlinkSync(file1);
    fs.unlinkSync(file2);
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should append to existing file when readFile/writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);

    const file1 = path.join(tmpDir, "example.txt");
    const file2 = path.join(tmpDir, "example2.txt");
    fs.writeFileSync(file1, "hello, World!!!");
    fs.writeFileSync(file2, "hello");
    assert.deepEqual(fs.existsSync(file1), true);
    assert.deepEqual(fs.existsSync(file2), true);

    let onExistsArgs = /** @type {any[]} */ ([]);
    const onExists = mockFunction((...args) => {
      onExistsArgs = args;
      return Promise.resolve(false);
    });
    const srcItem = _toFileListItem("example.txt", fs.lstatSync(file1));
    const existing = _toFileListItem("example2.txt", fs.lstatSync(file2));
    const buff = new Uint8Array(5);

    /** @type {(source: FileSource, target: FileTarget) => Promise<void>} */
    async function loop(source, target) {
      const bytesRead = await source.readNextBytes(buff);
      if (bytesRead === 0) {
        return target.setAttributes(srcItem);
      }
      return target
        .writeNextBytes(buff, bytesRead)
        .then((_) => loop(source, target));
    }

    //when
    const source = await apiImp.readFile(
      tmpDir,
      FileListItem("example.txt"),
      0.0
    );
    const target = await apiImp.writeFile(tmpDir, "example2.txt", onExists);
    if (!target) {
      assert.fail("target is undefined!");
    }
    await loop(source, target);
    await target.close();
    await source.close();

    //then
    assert.deepEqual(onExists.times, 1);
    assert.deepEqual(onExistsArgs, [existing]);
    const stats2 = fs.lstatSync(file2);
    assert.deepEqual(stats2.size, existing.size + srcItem.size);
    assert.deepEqual(
      toDateTimeStr(stats2.atimeMs),
      toDateTimeStr(srcItem.atimeMs)
    );
    assert.deepEqual(
      toDateTimeStr(stats2.mtimeMs),
      toDateTimeStr(srcItem.mtimeMs)
    );
    assert.deepEqual(
      fs.readFileSync(file2, {
        encoding: "utf8",
      }),
      "hellohello, World!!!"
    );

    //cleanup
    fs.unlinkSync(file1);
    assert.deepEqual(fs.existsSync(file2), true);
    await target.delete();
    assert.deepEqual(fs.existsSync(file2), false);
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should return undefined if skip existing file when writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);

    const file = path.join(tmpDir, "example2.txt");
    fs.writeFileSync(file, "hello");
    assert.deepEqual(fs.existsSync(file), true);

    let onExistsArgs = /** @type {any[]} */ ([]);
    const onExists = mockFunction((...args) => {
      onExistsArgs = args;
      return Promise.resolve(undefined);
    });
    const existing = _toFileListItem("example2.txt", fs.lstatSync(file));

    //when
    const result = await apiImp.writeFile(tmpDir, "example2.txt", onExists);

    //then
    assert.deepEqual(result, undefined);
    assert.deepEqual(onExists.times, 1);
    assert.deepEqual(onExistsArgs, [existing]);
    const stats2 = fs.lstatSync(file);
    assert.deepEqual(stats2.size, existing.size);
    assert.deepEqual(
      toDateTimeStr(stats2.atimeMs),
      toDateTimeStr(existing.atimeMs)
    );
    assert.deepEqual(
      toDateTimeStr(stats2.mtimeMs),
      toDateTimeStr(existing.mtimeMs)
    );
    assert.deepEqual(
      fs.readFileSync(file, {
        encoding: "utf8",
      }),
      "hello"
    );

    //cleanup
    fs.unlinkSync(file);
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should fail if other than EEXIST error when writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const error = Error("test error");

    const onExists = mockFunction();
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return Promise.reject(error);
    });
    const apiImp = new FSFileListApi({ ...fsMocks, open });

    //when
    let resError = null;
    try {
      await apiImp.writeFile(tmpDir, "example2.txt", onExists);
    } catch (error) {
      resError = error;
    }

    //then
    assert.deepEqual(resError === error, true);
    assert.deepEqual(onExists.times, 0);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [
      path.join(tmpDir, "example2.txt"),
      FSConstants.O_CREAT | FSConstants.O_WRONLY | FSConstants.O_EXCL,
    ]);

    //cleanup
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should fail if write error when writeFile", async () => {
    //given
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "farjs-test-"));
    assert.deepEqual(fs.existsSync(tmpDir), true);
    const filePath = path.join(tmpDir, "example2.txt");
    const handle = await fs.promises.open(
      filePath,
      FSConstants.O_CREAT | FSConstants.O_WRONLY | FSConstants.O_EXCL
    );

    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return Promise.resolve({ ...handle, write });
    });
    const buff = new Uint8Array(5);
    let writeArgs = /** @type {any[]} */ ([]);
    /** @type {any} */
    const write = mockFunction((...args) => {
      writeArgs = args;
      return Promise.resolve({ bytesWritten: 123, buffer: buff });
    });
    const onExists = mockFunction();
    const apiImp = new FSFileListApi({ ...fsMocks, open });
    const length = 3;
    const target = await apiImp.writeFile(tmpDir, "example2.txt", onExists);
    if (!target) {
      assert.fail("target is undefined!");
    }

    //when
    let resError = null;
    try {
      await target.writeNextBytes(buff, length);
    } catch (error) {
      resError = error;
    }

    //then
    assert.deepEqual(
      resError,
      Error(
        `File write error: bytesWritten(123) != expected(3), file: ${filePath}`
      )
    );
    assert.deepEqual(onExists.times, 0);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [
      filePath,
      FSConstants.O_CREAT | FSConstants.O_WRONLY | FSConstants.O_EXCL,
    ]);
    assert.deepEqual(write.times, 1);
    assert.deepEqual(writeArgs, [buff, 0, length, 0]);

    //cleanup
    await handle.close();
    fs.unlinkSync(filePath);
    fs.rmdirSync(tmpDir);
    assert.deepEqual(fs.existsSync(tmpDir), false);
  });

  it("should call fsService.readDisk when getDriveRoot", async () => {
    //given
    let readDiskArgs = /** @type {any[]} */ ([]);
    const readDisk = mockFunction((...args) => {
      readDiskArgs = args;
      return Promise.resolve(drive);
    });
    const fsService = new MockFSService({ readDisk });
    const path = "test path";
    /** @type {FSDisk} */
    const drive = { root: "/some/path", size: 0, free: 0, name: "SomeDrive" };

    const apiImp = new FSFileListApi(fsMocks, fsService);

    //when
    const result = await apiImp.getDriveRoot(path);

    //then
    assert.deepEqual(result, drive.root);
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(readDiskArgs, [path]);
  });

  it("should return file permissions", () => {
    //given
    /** @type {(s: string, c: string, f: number) => number} */
    const flag = (s, c, f) => (s === c ? f : 0);

    /** @type {(s: string) => number} */
    function of(s) {
      return (
        flag(s[0], "d", FSConstants.S_IFDIR) |
        flag(s[1], "r", FSConstants.S_IRUSR) |
        flag(s[2], "w", FSConstants.S_IWUSR) |
        flag(s[3], "x", FSConstants.S_IXUSR) |
        flag(s[4], "r", FSConstants.S_IRGRP) |
        flag(s[5], "w", FSConstants.S_IWGRP) |
        flag(s[6], "x", FSConstants.S_IXGRP) |
        flag(s[7], "r", FSConstants.S_IROTH) |
        flag(s[8], "w", FSConstants.S_IWOTH) |
        flag(s[9], "x", FSConstants.S_IXOTH)
      );
    }

    /** @type {(s: string) => string} */
    function expected(s) {
      return process.platform === "win32" ? `${s.slice(0, 3)}-------` : s;
    }

    //when & then
    const getPerms = FSFileListApi._getPermissions;
    assert.deepEqual(getPerms(0), "----------");
    assert.deepEqual(getPerms(of("d---------")), expected("d---------"));
    assert.deepEqual(getPerms(of("-r--------")), expected("-r--------"));
    assert.deepEqual(getPerms(of("--w-------")), expected("--w-------"));
    assert.deepEqual(getPerms(of("---x------")), expected("---x------"));
    assert.deepEqual(getPerms(of("----r-----")), expected("----r-----"));
    assert.deepEqual(getPerms(of("-----w----")), expected("-----w----"));
    assert.deepEqual(getPerms(of("------x---")), expected("------x---"));
    assert.deepEqual(getPerms(of("-------r--")), expected("-------r--"));
    assert.deepEqual(getPerms(of("--------w-")), expected("--------w-"));
    assert.deepEqual(getPerms(of("---------x")), expected("---------x"));
    assert.deepEqual(getPerms(of("drwxrwxrwx")), expected("drwxrwxrwx"));
  });
});

/** @type {(dtimeMs: number) => string} */
function toDateTimeStr(dtimeMs) {
  const date = new Date(dtimeMs);
  return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
}

/** @type {(path: string, isDir: boolean) => void} */
function del(path, isDir) {
  if (fs.existsSync(path)) {
    if (isDir) fs.rmdirSync(path);
    else fs.unlinkSync(path);
  }
}
