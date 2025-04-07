/**
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { QuickViewParams, QuickViewDirProps } from "../../../viewer/quickview/QuickViewDir.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";
import QuickViewDir from "../../../viewer/quickview/QuickViewDir.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

QuickViewDir.statusPopupComp = mockComponent(StatusPopup);
QuickViewDir.textLineComp = mockComponent(TextLine);

const { statusPopupComp, textLineComp } = QuickViewDir;

const currComp = () => null;

describe("QuickViewDir.test.mjs", () => {
  it("should update params with calculated stats when item name changes", async () => {
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
    const currItem = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      currItem,
      { ...FileListItem("file 1"), size: 10 },
    ]);
    const state = { ...FileListState(), currDir };
    /** @type {QuickViewParams} */
    const params = {
      name: currItem.name,
      parent: currDir.path,
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<QuickViewParams>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {(f: (data: readonly PanelStackItem<QuickViewParams>[]) => readonly PanelStackItem<QuickViewParams>[]) => void} */
    const stackUpdater = (f) => {
      stackState = f(stackState);
    };
    const stack = new PanelStack(false, stackState, stackUpdater);
    const props = { dispatch, actions, state, stack, width: 25, currItem };
    const renderer = TestRenderer.create(
      withThemeContext(h(QuickViewDir, props))
    );
    await Promise.resolve();
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);

    //when & then
    const newParams = { ...params, name: "" };
    stack.update((_) => _.withState(newParams));
    const newStack = new PanelStack(false, stackState, stackUpdater);
    TestRenderer.act(() => {
      renderer.update(
        withThemeContext(h(QuickViewDir, { ...props, stack: newStack }))
      );
    });
    const popupProps = renderer.root.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);

    //when & then
    /** @type {boolean} */
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 122 },
      { ...FileListItem("file 1"), size: 1 },
    ]);
    assert.deepEqual(result, true);
    resolve(result);
    await scanDirsP;

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(stackState[0].state, {
      ...params,
      name: "dir 1",
      parent: currDir.path,
      folders: 1,
      files: 2,
      filesSize: 123,
    });
  });

  it("should update params with calculated stats when curr path changes", async () => {
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
    const currItem = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      currItem,
      { ...FileListItem("file 1"), size: 10 },
    ]);
    const state = { ...FileListState(), currDir };
    /** @type {QuickViewParams} */
    const params = {
      name: currItem.name,
      parent: currDir.path,
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<QuickViewParams>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {(f: (data: readonly PanelStackItem<QuickViewParams>[]) => readonly PanelStackItem<QuickViewParams>[]) => void} */
    const stackUpdater = (f) => {
      stackState = f(stackState);
    };
    const stack = new PanelStack(false, stackState, stackUpdater);
    const props = { dispatch, actions, state, stack, width: 25, currItem };
    const renderer = TestRenderer.create(
      withThemeContext(h(QuickViewDir, props))
    );
    await Promise.resolve();
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);

    //when & then
    const newParams = { ...params, parent: "" };
    stack.update((_) => _.withState(newParams));
    const newStack = new PanelStack(false, stackState, stackUpdater);
    TestRenderer.act(() => {
      renderer.update(
        withThemeContext(h(QuickViewDir, { ...props, stack: newStack }))
      );
    });
    const popupProps = renderer.root.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);

    //when & then
    /** @type {boolean} */
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 122 },
      { ...FileListItem("file 1"), size: 1 },
    ]);
    assert.deepEqual(result, true);
    resolve(result);
    await scanDirsP;

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(stackState[0].state, {
      ...params,
      name: "dir 1",
      parent: currDir.path,
      folders: 1,
      files: 2,
      filesSize: 123,
    });
  });

  it("should handle cancel action and hide StatusPopup when onClose", async () => {
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
    const currItem = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      currItem,
      { ...FileListItem("file 1"), size: 10 },
    ]);
    const state = { ...FileListState(), currDir };
    /** @type {QuickViewParams} */
    const params = {
      name: "",
      parent: "",
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<QuickViewParams>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {(f: (data: readonly PanelStackItem<QuickViewParams>[]) => readonly PanelStackItem<QuickViewParams>[]) => void} */
    const stackUpdater = (f) => {
      stackState = f(stackState);
    };
    const stack = new PanelStack(false, stackState, stackUpdater);
    const props = { dispatch, actions, state, stack, width: 25, currItem };
    const renderer = TestRenderer.create(
      withThemeContext(h(QuickViewDir, props))
    );
    await Promise.resolve();

    const popupProps = renderer.root.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems, onNextDir] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);

    //when & then
    popupProps.onClose();
    /** @type {boolean} */
    const result = onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 2"), size: 122 },
      { ...FileListItem("file 1"), size: 1 },
    ]);
    assert.deepEqual(result, false);
    resolve(result);
    await scanDirsP;

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(stackState[0].state, {
      ...params,
      name: "dir 1",
      parent: currDir.path,
      folders: 0,
      files: 0,
      filesSize: 0,
    });
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
    const currItem = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      currItem,
      { ...FileListItem("file 1"), size: 10 },
    ]);
    const state = { ...FileListState(), currDir };
    /** @type {QuickViewParams} */
    const params = {
      name: "",
      parent: "",
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<QuickViewParams>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {(f: (data: readonly PanelStackItem<QuickViewParams>[]) => readonly PanelStackItem<QuickViewParams>[]) => void} */
    const stackUpdater = (f) => {
      stackState = f(stackState);
    };
    const stack = new PanelStack(false, stackState, stackUpdater);
    const props = { dispatch, actions, state, stack, width: 25, currItem };
    const renderer = TestRenderer.create(
      withThemeContext(h(QuickViewDir, props))
    );
    await Promise.resolve();

    const popupProps = renderer.root.findByType(statusPopupComp).props;
    assert.deepEqual(popupProps.text, "Scanning the folder\ndir 1");
    const [resPath, resItems] = scanDirsArgs;
    assert.deepEqual(resPath, currDir.path);
    assert.deepEqual(resItems, [currDir.items[0]]);
    const error = Error("test error");

    //when
    reject(error);
    await Promise.resolve();

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(stackState[0].state, {
      ...params,
      name: "dir 1",
      parent: currDir.path,
      folders: 0,
      files: 0,
      filesSize: 0,
    });
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

  it("should render component with existing params", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const currItem = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      currItem,
      { ...FileListItem("file 1"), size: 10 },
    ]);
    const state = { ...FileListState(), currDir };
    /** @type {QuickViewParams} */
    const params = {
      name: currItem.name,
      parent: currDir.path,
      folders: 1,
      files: 2,
      filesSize: 3,
    };
    /** @type {readonly PanelStackItem<QuickViewParams>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    const stack = new PanelStack(false, stackState, (f) => {
      stackState = /** @type {readonly PanelStackItem<QuickViewParams>[]} */ (
        f(stackState)
      );
    });
    const props = { dispatch, actions, state, stack, width: 25, currItem };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(QuickViewDir, props))
    ).root;

    //then
    assertQuickViewDir(result, props, params);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {QuickViewDirProps} props
 * @param {QuickViewParams} params
 */
function assertQuickViewDir(result, props, params) {
  assert.deepEqual(QuickViewDir.displayName, "QuickViewDir");

  const theme = FileListTheme.defaultTheme.fileList;

  assertComponents(
    result.children,
    h("text", {
      left: 2,
      top: 2,
      style: theme.regularItem,
      content: `Folder

Contains:

Folders
Files
Files size`,
    }),

    h(textLineComp, {
      align: TextAlign.left,
      left: 12,
      top: 2,
      width: props.width - 14,
      text: `"${props.currItem.name}"`,
      style: theme.regularItem,
      padding: 0,
    }),

    h("text", {
      left: 15,
      top: 6,
      style: theme.selectedItem,
      content: `${formatSize(params.folders)}
${formatSize(params.files)}
${formatSize(params.filesSize)}`,
    })
  );
}
