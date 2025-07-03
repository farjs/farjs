import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import FSFoldersHistory from "../../fs/FSFoldersHistory.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const { foldersHistoryKind } = FSFoldersHistory;

describe("FSFoldersHistory.test.mjs", () => {
  it("should not save current dir if it is empty", async () => {
    //given
    const props = { currDirPath: "" };
    const get = mockFunction();
    const provider = new MockHistoryProvider({ get });

    //when
    const renderer = TestRenderer.create(
      withHistoryProvider(h(FSFoldersHistory, props), provider)
    );

    //then
    assert.deepEqual(get.times, 0);

    assert.deepEqual(renderer.root.children.length, 0);
    assert.deepEqual(FSFoldersHistory.displayName, "FSFoldersHistory");
  });

  it("should save current dir if it is non-empty", async () => {
    //given
    const props = { currDirPath: "dir 1" };
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    //when
    const renderer = TestRenderer.create(
      withHistoryProvider(h(FSFoldersHistory, props), provider)
    );

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [foldersHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: props.currDirPath }]);

    assert.deepEqual(renderer.root.children.length, 0);
    assert.deepEqual(FSFoldersHistory.displayName, "FSFoldersHistory");
  });

  it("should not save current dir if it is not changed", async () => {
    //given
    const props = { currDirPath: "dir 1" };
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(FSFoldersHistory, props), provider)
    );
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [foldersHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: props.currDirPath }]);
    const updatedProps = { ...props };

    //when
    TestRenderer.act(() => {
      renderer.update(
        withHistoryProvider(h(FSFoldersHistory, updatedProps), provider)
      );
    });

    //then
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(get.times, 1);
    assert.deepEqual(save.times, 1);
  });

  it("should save current dir if it is changed", async () => {
    //given
    const props = { currDirPath: "dir 1" };
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(FSFoldersHistory, props), provider)
    );
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [foldersHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: props.currDirPath }]);
    const updatedProps = { currDirPath: "dir 2" };

    //when
    TestRenderer.act(() => {
      renderer.update(
        withHistoryProvider(h(FSFoldersHistory, updatedProps), provider)
      );
    });

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 2);
    assert.deepEqual(getArgs, [foldersHistoryKind]);
    assert.deepEqual(save.times, 2);
    assert.deepEqual(saveArgs, [{ item: updatedProps.currDirPath }]);
  });
});
