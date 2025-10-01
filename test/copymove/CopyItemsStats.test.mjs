/**
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { CopyItemsStatsProps } from "../../copymove/CopyItemsStats.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import CopyItemsStats from "../../copymove/CopyItemsStats.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

CopyItemsStats.statusPopupComp = mockComponent(StatusPopup);

const { statusPopupComp } = CopyItemsStats;

describe("CopyItemsStats.test.mjs", () => {
  it("should call onCancel when onClose in popup", async () => {
    //given
    const onDone = mockFunction();
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return Promise.resolve(false);
    });
    const onCancel = mockFunction();
    const dispatch = mockFunction();
    const actions = new MockFileListActions({ scanDirs });
    const props = getCopyItemsStatsProps({
      dispatch,
      actions,
      onDone,
      onCancel,
    });
    const comp = (
      await actAsync(() => {
        return TestRenderer.create(h(CopyItemsStats, props));
      })
    ).root;
    assert.deepEqual(scanDirs.times, 1);
    const [scanDir, dirItems] = scanDirsArgs;
    assert.deepEqual(scanDir, props.fromPath);
    assert.deepEqual(dirItems, [props.items[0]]);
    const statusPopup = comp.findByType(statusPopupComp).props;

    //when
    statusPopup.onClose();

    //then
    assert.deepEqual(onCancel.times, 1);
    assert.deepEqual(onDone.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should call onDone with calculated total size", async () => {
    //given
    let onDoneArgs = /** @type {any[]} */ ([]);
    const onDone = mockFunction((...args) => (onDoneArgs = args));
    let resolve = /** @type {any} */ (null);
    const scanDirsP = new Promise((res) => (resolve = res));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const onCancel = mockFunction();
    const dispatch = mockFunction();
    const actions = new MockFileListActions({ scanDirs });
    const props = getCopyItemsStatsProps({
      dispatch,
      actions,
      items: [
        FileListItem("dir 1", true),
        { ...FileListItem("file 1"), size: 10 },
      ],
      title: "Move",
      onDone,
      onCancel,
    });

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(CopyItemsStats, props));
    });

    //then
    assert.deepEqual(scanDirs.times, 1);
    assertCopyItemsStats(renderer.root, props);
    const [scanDir, dirItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(scanDir, props.fromPath);
    assert.deepEqual(dirItems, [props.items[0]]);

    //when & then
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 123 },
    ]);
    assert.deepEqual(result, true);

    //when & then
    resolve(true);
    await scanDirsP;
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(onDoneArgs, [133]);
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should handle cancel action when unmount", async () => {
    //given
    const onDone = mockFunction();
    let resolve = /** @type {any} */ (null);
    const scanDirsP = new Promise((res) => (resolve = res));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const onCancel = mockFunction();
    const dispatch = mockFunction();
    const actions = new MockFileListActions({ scanDirs });
    const props = getCopyItemsStatsProps({
      dispatch,
      actions,
      onDone,
      onCancel,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(h(CopyItemsStats, props));
    });
    assert.deepEqual(scanDirs.times, 1);
    const [scanDir, dirItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(scanDir, props.fromPath);
    assert.deepEqual(dirItems, [props.items[0]]);

    //when & then
    TestRenderer.act(() => {
      renderer.unmount();
    });
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 123 },
    ]);
    assert.deepEqual(result, false);

    //when & then
    resolve(false);
    await scanDirsP;
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onDone.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should dispatch actions when failure", async () => {
    //given
    const onDone = mockFunction();
    /** @type {(e: Error) => void} */
    let reject = () => {};
    const scanDirsP = new Promise((_, rej) => (reject = rej));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const onCancel = mockFunction();
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions({ scanDirs });
    const props = getCopyItemsStatsProps({
      dispatch,
      actions,
      onDone,
      onCancel,
    });
    await actAsync(() => {
      return TestRenderer.create(h(CopyItemsStats, props));
    });
    assert.deepEqual(scanDirs.times, 1);
    const [scanDir, dirItems] = scanDirsArgs;
    assert.deepEqual(scanDir, props.fromPath);
    assert.deepEqual(dirItems, [props.items[0]]);
    const error = Error("test error");

    //when
    await actAsync(() => {
      reject(error);
    });

    //then
    assert.deepEqual(onDone.times, 0);
    assert.deepEqual(onCancel.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Copy dir scan");
    let resultError = null;
    try {
      await action.task.result;
    } catch (e) {
      resultError = e;
    }
    assert.deepEqual(resultError, error);
  });
});

/**
 * @param {Partial<CopyItemsStatsProps>} props
 * @returns {CopyItemsStatsProps}
 */
function getCopyItemsStatsProps(props = {}) {
  return {
    dispatch: mockFunction(),
    actions: new MockFileListActions(),
    fromPath: "/folder",
    items: [FileListItem("dir 1", true)],
    title: "Copy",
    onDone: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {CopyItemsStatsProps} props
 */
function assertCopyItemsStats(result, props) {
  assert.deepEqual(CopyItemsStats.displayName, "CopyItemsStats");

  assertComponents(
    result.children,
    h(statusPopupComp, {
      text: "Calculating total size\ndir 1",
      title: props.title,
      onClose: mockFunction(),
    })
  );
}
