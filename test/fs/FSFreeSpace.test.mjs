/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { FSDisk } from "../../fs/FSDisk.mjs"
 * @import { FSFreeSpaceProps } from "../../fs/FSFreeSpace.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync } from "react-assert";
import mockFunction from "mock-fn";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import MockFSService from "../../fs/MockFSService.mjs";
import FSFreeSpace from "../../fs/FSFreeSpace.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FSFreeSpace.test.mjs", () => {
  it("should dispatch action when readDisk returns value", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<FSDisk>} */
    const readDiskP = Promise.resolve({
      root: "/Volumes/TestDrive",
      size: 123,
      free: 456,
      name: "TestDrive",
    });
    let readDiskArgs = /** @type {any[]} */ ([]);
    const readDisk = mockFunction((...args) => {
      readDiskArgs = args;
      return readDiskP;
    });
    FSFreeSpace.fsService = new MockFSService({ readDisk });
    const props = getFSFreeSpaceProps({ dispatch });

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(FSFreeSpace, props));
    });

    //then
    await readDiskP;
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(readDiskArgs, [props.currDir.path]);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [
      {
        action: "FileListDiskSpaceUpdatedAction",
        diskSpace: 456,
      },
    ]);

    assertFSFreeSpace(renderer.root);
  });

  it("should not dispatch action when readDisk returns undefined", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {Promise<FSDisk | undefined>} */
    const readDiskP = Promise.resolve(undefined);
    let readDiskArgs = /** @type {any[]} */ ([]);
    const readDisk = mockFunction((...args) => {
      readDiskArgs = args;
      return readDiskP;
    });
    FSFreeSpace.fsService = new MockFSService({ readDisk });
    const props = getFSFreeSpaceProps({ dispatch });

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(FSFreeSpace, props));
    });

    //then
    await readDiskP;
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(readDiskArgs, [props.currDir.path]);
    assert.deepEqual(dispatch.times, 0);

    assertFSFreeSpace(renderer.root);
  });

  it("should not dispatch action when readDisk fails", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {Promise<FSDisk | undefined>} */
    const readDiskP = Promise.reject(Error("test error"));
    let readDiskArgs = /** @type {any[]} */ ([]);
    const readDisk = mockFunction((...args) => {
      readDiskArgs = args;
      return readDiskP;
    });
    FSFreeSpace.fsService = new MockFSService({ readDisk });
    const props = getFSFreeSpaceProps({ dispatch });

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(FSFreeSpace, props));
    });

    //then
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(readDiskArgs, [props.currDir.path]);
    assert.deepEqual(dispatch.times, 0);

    assertFSFreeSpace(renderer.root);
  });

  it("should not call readDisk if currDir is not changed when re-render", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<FSDisk>} */
    const readDiskP = Promise.resolve({
      root: "/Volumes/TestDrive",
      size: 123,
      free: 456,
      name: "TestDrive",
    });
    let readDiskArgs = /** @type {any[]} */ ([]);
    const readDisk = mockFunction((...args) => {
      readDiskArgs = args;
      return readDiskP;
    });
    FSFreeSpace.fsService = new MockFSService({ readDisk });
    const props = getFSFreeSpaceProps({ dispatch });
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(FSFreeSpace, props));
    });
    await readDiskP;
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(readDiskArgs, [props.currDir.path]);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [
      {
        action: "FileListDiskSpaceUpdatedAction",
        diskSpace: 456,
      },
    ]);

    //when
    await TestRenderer.act(async () => {
      renderer.update(h(FSFreeSpace, { ...props }));
    });

    //then
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(readDisk.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assertFSFreeSpace(renderer.root);
  });

  it("should dispatch action only for the same (current) dir instance", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {any} */
    let resolve1;
    /** @type {any} */
    let resolve2;
    /** @type {Promise<FSDisk>} */
    const readDiskP1 = new Promise((resolve) => (resolve1 = resolve));
    /** @type {Promise<FSDisk>} */
    const readDiskP2 = new Promise((resolve) => (resolve2 = resolve));
    const disk1 = {
      root: "/",
      size: 123,
      free: 456,
      name: "/",
    };
    const disk2 = {
      root: "/",
      size: 124,
      free: 457,
      name: "/2",
    };
    const props = getFSFreeSpaceProps({ dispatch });
    const props2 = getFSFreeSpaceProps({
      dispatch,
      currDir: { ...props.currDir, path: "/2" },
    });
    const readDisk = mockFunction((path) => {
      return path === props.currDir.path ? readDiskP1 : readDiskP2;
    });
    FSFreeSpace.fsService = new MockFSService({ readDisk });
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(FSFreeSpace, props));
    });

    //when
    await TestRenderer.act(async () => {
      renderer.update(h(FSFreeSpace, props2));
    });

    //then
    assert.deepEqual(readDisk.times, 2);
    resolve1(disk1);
    resolve2(disk2);
    await readDiskP1;
    await readDiskP2;
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [
      {
        action: "FileListDiskSpaceUpdatedAction",
        diskSpace: disk2.free,
      },
    ]);
    assertFSFreeSpace(renderer.root);
  });
});

/**
 * @param {Partial<FSFreeSpaceProps>} props
 * @returns {FSFreeSpaceProps}
 */
function getFSFreeSpaceProps(props = {}) {
  return {
    dispatch: mockFunction(),
    currDir: FileListDir("/test", false, []),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertFSFreeSpace(result) {
  assert.deepEqual(FSFreeSpace.displayName, "FSFreeSpace");

  assert.deepEqual(result.children.length, 0);
}
