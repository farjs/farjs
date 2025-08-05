/**
 * @typedef {import("../../fs/FSService.mjs").FSService} FSService
 */
import os from "node:os";
import nodePath from "node:path";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import { stripSuffix } from "@farjs/filelist/utils.mjs";
import FSService from "../../fs/FSService.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FSService.test.mjs", () => {
  it("should fail if error when openItem", async () => {
    //given
    const error = Error("test error");
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("darwin", { exec });
    const parent = "test dir";
    const item = "..";

    //when
    const resP = service.openItem(parent, item);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `open "."`,
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);
    callback(error, "test stdout", "test stderr");
    let catchedError = null;

    try {
      //when
      await resP;
    } catch (e) {
      catchedError = e;
    }

    //then
    assert.deepEqual(catchedError === error, true);
  });

  it("should open item in default app on Mac OS", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("darwin", { exec });
    const parent = "test dir";
    const item = "..";

    //when
    const resP = service.openItem(parent, item);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `open "."`,
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);

    //when & then
    callback(undefined, "test stdout");
    await resP;
  });

  it("should open item in default app on Windows", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });
    const parent = "test dir";
    const item = "file 1";

    //when
    const resP = service.openItem(parent, item);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `start "" "${item}"`,
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);

    //when & then
    callback(undefined, "test stdout");
    await resP;
  });

  it("should open item in default app on Linux", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("linux", { exec });
    const parent = "test dir";
    const item = "file 1";

    //when
    const resP = service.openItem(parent, item);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `xdg-open "${item}"`,
      {
        cwd: parent,
        windowsHide: true,
      },
    ]);

    //when & then
    callback(undefined, "test stdout");
    await resP;
  });

  it("should fail if error when readDisk", async () => {
    //given
    const error = Error("test error");
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });
    const path = os.homedir();
    const root = stripSuffix(nodePath.parse(path).root, "\\");

    //when
    const resP = service.readDisk(path);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `wmic logicaldisk where "Caption='${root}'" get Caption,VolumeName,FreeSpace,Size`,
      {
        cwd: path,
        windowsHide: true,
      },
    ]);
    callback(error, "test stdout", "test stderr");
    let catchedError = null;

    try {
      //when
      await resP;
    } catch (e) {
      catchedError = e;
    }

    //then
    assert.deepEqual(catchedError === error, true);
  });

  it("should return undefined if not parseable output when readDisk", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });
    const path = os.homedir();
    const root = stripSuffix(nodePath.parse(path).root, "\\");

    //when
    const resP = service.readDisk(path);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `wmic logicaldisk where "Caption='${root}'" get Caption,VolumeName,FreeSpace,Size`,
      {
        cwd: path,
        windowsHide: true,
      },
    ]);

    //when
    callback(undefined, "test stdout");
    const result = await resP;

    //then
    assert.deepEqual(result, undefined);
  });

  it("should read disk info on Windows", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });
    const path = os.homedir();
    const root = stripSuffix(nodePath.parse(path).root, "\\");
    const output = `Caption  FreeSpace     Size          VolumeName
C:       81697124352   156595318784  SYSTEM
`;

    //when
    const resP = service.readDisk(path);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `wmic logicaldisk where "Caption='${root}'" get Caption,VolumeName,FreeSpace,Size`,
      {
        cwd: path,
        windowsHide: true,
      },
    ]);

    //when
    callback(undefined, output);
    const result = await resP;

    //then
    assert.deepEqual(result, {
      root: "C:",
      size: 156595318784,
      free: 81697124352,
      name: "SYSTEM",
    });
  });

  it("should read disk info on Mac OS/Linux", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("darwin", { exec });
    const path = os.homedir();
    const output = `Filesystem   1024-blocks      Used Available Capacity  Mounted on
/dev/disk1s1   244912536 202577024  40612004    84%    /
`;

    //when
    const resP = service.readDisk(path);

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      `df -kP "${path}"`,
      {
        cwd: path,
        windowsHide: true,
      },
    ]);

    //when
    callback(undefined, output);
    const result = await resP;

    //then
    assert.deepEqual(result, {
      root: "/",
      size: 250790436864,
      free: 41586692096,
      name: "/",
    });
  });

  it("should fail if error when readDisks", async () => {
    //given
    const error = Error("test error");
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });

    //when
    const resP = service.readDisks();

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      "wmic logicaldisk get Caption,VolumeName,FreeSpace,Size",
      {
        windowsHide: true,
      },
    ]);

    //when
    callback(error, "test output");
    let catchedError = null;

    try {
      //when
      await resP;
    } catch (e) {
      catchedError = e;
    }

    //then
    assert.deepEqual(catchedError === error, true);
  });

  it("should read disks on Windows", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("win32", { exec });
    const output = `Caption  FreeSpace     Size          VolumeName
C:       81697124352   156595318784  SYSTEM
`;

    //when
    const resP = service.readDisks();

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      "wmic logicaldisk get Caption,VolumeName,FreeSpace,Size",
      {
        windowsHide: true,
      },
    ]);

    //when
    callback(undefined, output);
    const results = await resP;

    //then
    assert.deepEqual(results, [
      {
        root: "C:",
        size: 156595318784,
        free: 81697124352,
        name: "SYSTEM",
      },
    ]);
  });

  it("should read disks on Mac OS/Linux", async () => {
    //given
    let execArgs = /** @type {any[]} */ ([]);
    const exec = mockFunction((...args) => {
      execArgs = args;
    });
    const service = FSService("darwin", { exec });
    const output = `Filesystem   1024-blocks      Used Available Capacity  Mounted on
/dev/disk1s1   244912536 202577024  40612004    84%    /
devfs                234       234         0   100%    /dev
/dev/disk1s3   244912536   4194424  59259185     8%    /System/Volumes/VM
/dev/disk1s4   244912536   4194424  59259180     7%    /private/var/vm
map -hosts             0         0         0   100%    /net
map auto_home          0         0         0   100%    /home
/dev/disk2s1     1957408     14752   1942656     1%    /Volumes/FLASHDRIVE
/dev/vda1        1957408     14752   1942656    45%    /etc/hosts
tmpfs            1018208         0   1018208     0%    /sys/firmware
`;

    //when
    const resP = service.readDisks();

    //then
    assert.deepEqual(exec.times, 1);
    const callback = execArgs.pop();
    assert.deepEqual(execArgs, [
      "df -kP",
      {
        windowsHide: true,
      },
    ]);

    //when
    callback(undefined, output);
    const results = await resP;

    //then
    assert.deepEqual(results, [
      {
        root: "/",
        size: 250790436864,
        free: 41586692096,
        name: "/",
      },
      {
        root: "/Volumes/FLASHDRIVE",
        size: 2004385792,
        free: 1989279744,
        name: "FLASHDRIVE",
      },
    ]);
  });
});
