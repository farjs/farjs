/**
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListDirUpdatedAction } from "@farjs/filelist/FileListActions.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import ViewItemsPopup from "../../viewer/ViewItemsPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewItemsPopup.statusPopupComp = mockComponent(StatusPopup);

const { statusPopupComp } = ViewItemsPopup;

describe("ViewItemsPopup.test.mjs", () => {
  it("should dispatch action with calculated items sizes", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const scanDirsP = new Promise((res) => (resolve = res));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const actions = new MockFileListActions({ scanDirs });
    const dir1 = FileListItem("dir 1", true);
    const file1 = { ...FileListItem("file 1"), size: 10 };
    const file2 = { ...FileListItem("file 2"), size: 11 };
    const currDir = FileListDir("/folder", false, [dir1, file1, file2]);
    const state = {
      ...FileListState(),
      currDir,
      selectedNames: new Set(["dir 1", "file 1"]),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(dispatch, actions, state);
    const viewItemsPopup = ViewItemsPopup(data);
    const comp = (
      await actAsync(() => {
        return TestRenderer.create(h(viewItemsPopup, props));
      })
    ).root;
    await Promise.resolve();

    const popupProps = comp.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);

    //when & then
    /** @type {boolean} */
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 123 },
    ]);
    assert.deepEqual(result, true);
    resolve(result);
    await scanDirsP;
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();

    //then
    /** @type {FileListDirUpdatedAction} */
    const action = {
      action: "FileListDirUpdatedAction",
      currDir: {
        ...currDir,
        items: [
          { ...dir1, size: 123 },
          { ...file1, size: 10 },
          { ...file2, size: 11 },
        ],
      },
    };
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
  });

  it("should handle cancel action when onClose", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const scanDirsP = new Promise((res) => (resolve = res));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const actions = new MockFileListActions({ scanDirs });
    const currDir = FileListDir("/folder", false, [
      FileListItem("dir 1", true),
    ]);
    const state = {
      ...FileListState(),
      currDir,
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(dispatch, actions, state);
    const viewItemsPopup = ViewItemsPopup(data);
    const comp = (
      await actAsync(() => {
        return TestRenderer.create(h(viewItemsPopup, props));
      })
    ).root;
    await Promise.resolve();

    const popupProps = comp.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);

    //when & then
    popupProps.onClose();
    /** @type {boolean} */
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 123 },
    ]);
    assert.deepEqual(result, false);
    resolve(result);
    await scanDirsP;
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should dispatch action when failure", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {(e: Error) => void} */
    let reject = () => {};
    const scanDirsP = new Promise((_, rej) => (reject = rej));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return scanDirsP;
    });
    const actions = new MockFileListActions({ scanDirs });
    const currDir = FileListDir("/folder", false, [
      FileListItem("dir 1", true),
    ]);
    const state = {
      ...FileListState(),
      currDir,
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(dispatch, actions, state);
    const viewItemsPopup = ViewItemsPopup(data);
    const comp = (
      await actAsync(() => {
        return TestRenderer.create(h(viewItemsPopup, props));
      })
    ).root;
    await Promise.resolve();

    const popupProps = comp.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);
    const error = Error("test error");

    //when
    reject(error);
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();

    //then
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    let resultError = null;
    try {
      await action.task.result;
    } catch (e) {
      resultError = e;
    }
    assert.deepEqual(resultError, error);
  });

  it("should render StatusPopup component", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(dispatch, actions, state);
    const viewItemsPopup = ViewItemsPopup(data);

    //when
    const result = TestRenderer.create(h(viewItemsPopup, props)).root;

    //then
    assertViewItemsPopup(result, viewItemsPopup);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} viewItemsPopup
 */
function assertViewItemsPopup(result, viewItemsPopup) {
  assert.deepEqual(viewItemsPopup.displayName, "ViewItemsPopup");

  assertComponents(
    result.children,
    h(statusPopupComp, {
      text: "Scanning the folder\n",
      title: "View",
    })
  );
}
