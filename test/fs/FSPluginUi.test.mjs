/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 * @import { FSPluginUiOptions } from "../../fs/FSPluginUi.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import DriveController from "../../fs/popups/DriveController.mjs";
import FoldersHistoryController from "../../fs/popups/FoldersHistoryController.mjs";
import FolderShortcutsController from "../../fs/popups/FolderShortcutsController.mjs";
import FSPluginUi from "../../fs/FSPluginUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FSPluginUi.drive = mockComponent(DriveController);
FSPluginUi.foldersHistory = mockComponent(FoldersHistoryController);
FSPluginUi.folderShortcuts = mockComponent(FolderShortcutsController);

const { drive, foldersHistory, folderShortcuts } = FSPluginUi;

const fsComp = () => null;
const otherComp = () => null;

describe("FSPluginUi.test.mjs", () => {
  it("should dispatch TaskAction when onChangeDir in active panel", () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const onClose = mockFunction();
    let changeDirArgs = /** @type {any[]} */ ([]);
    const changeDir = mockFunction((...args) => {
      changeDirArgs = args;
      return action;
    });
    const actions = new MockFileListActions({ changeDir });
    const props = { dispatch, onClose };
    const pluginUi = FSPluginUi();
    const currState = {
      ...FileListState(),
      currDir: FileListDir("C:/test", false, []),
    };
    const currFsItem = new PanelStackItem(fsComp, dispatch, actions, currState);
    /** @type {readonly PanelStackItem<any>[]} */
    let currStackState = [new PanelStackItem(otherComp), currFsItem];
    const currStack = new PanelStack(true, currStackState, (f) => {
      currStackState = f(currStackState);
    });
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(currStack),
      WithStacksData(otherStack)
    );
    const comp = TestRenderer.create(
      withStacksContext(h(pluginUi, props), stacksProps)
    ).root;
    const foldersHistoryProps = comp.findByType(foldersHistory).props;
    const action = TaskAction(
      Task("Changing Dir", Promise.resolve(FileListDir("/", true, [])))
    );
    const dir = "test/dir";

    //when
    foldersHistoryProps.onChangeDir(dir);

    //then
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
    assert.deepEqual(changeDir.times, 1);
    assert.deepEqual(changeDirArgs, [dispatch, "", dir]);
    assert.deepEqual(currStackState, [currFsItem]);
  });

  it("should dispatch TaskAction when onChangeDir in Drive popup", () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const onClose = mockFunction();
    let changeDirArgs = /** @type {any[]} */ ([]);
    const changeDir = mockFunction((...args) => {
      changeDirArgs = args;
      return action;
    });
    const actions = new MockFileListActions({ changeDir });
    const props = { dispatch, onClose };
    const pluginUi = FSPluginUi();
    const currState = {
      ...FileListState(),
      currDir: FileListDir("C:/test", false, []),
    };
    const currStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, currState)],
      mockFunction()
    );
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(otherStack),
      WithStacksData(currStack)
    );
    const comp = TestRenderer.create(
      withStacksContext(h(pluginUi, props), stacksProps)
    ).root;
    const driveProps = comp.findByType(drive).props;
    const action = TaskAction(
      Task("Changing Dir", Promise.resolve(FileListDir("/", true, [])))
    );
    const dir = "test/dir";

    //when
    driveProps.onChangeDir(dir, false);

    //then
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
    assert.deepEqual(changeDir.times, 1);
    assert.deepEqual(changeDirArgs, [dispatch, "", dir]);
  });

  it("should not dispatch TaskAction if same dir when onChangeDir", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const changeDir = mockFunction();
    const actions = new MockFileListActions({ changeDir });
    const props = { dispatch, onClose };
    const pluginUi = FSPluginUi();
    const currState = {
      ...FileListState(),
      currDir: FileListDir("C:/test", false, []),
    };
    const currFsItem = new PanelStackItem(fsComp, dispatch, actions, currState);
    /** @type {readonly PanelStackItem<any>[]} */
    let currStackState = [new PanelStackItem(otherComp), currFsItem];
    const currStack = new PanelStack(true, currStackState, (f) => {
      currStackState = f(currStackState);
    });
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(otherStack),
      WithStacksData(currStack)
    );
    const comp = TestRenderer.create(
      withStacksContext(h(pluginUi, props), stacksProps)
    ).root;
    const foldersHistoryProps = comp.findByType(foldersHistory).props;
    const dir = currState.currDir.path;

    //when
    foldersHistoryProps.onChangeDir(dir);

    //then
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(changeDir.times, 0);
    assert.deepEqual(currStackState, [currFsItem]);
  });

  it("should render component", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, undefined, undefined)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(otherComp)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    /** @type {(options: FSPluginUiOptions) => void} */
    function check(options) {
      //given
      const pluginUi = FSPluginUi(options);

      //when
      const result = TestRenderer.create(
        withStacksContext(h(pluginUi, props), stacksProps)
      ).root;

      //then
      assertFSPluginUi(result, pluginUi, props, options);
    }

    //when & then
    check({
      showDrivePopupOnLeft: true,
      showFoldersHistoryPopup: false,
      showFolderShortcutsPopup: false,
    });
    check({
      showDrivePopupOnLeft: false,
      showFoldersHistoryPopup: true,
      showFolderShortcutsPopup: false,
    });
    check({
      showDrivePopupOnLeft: false,
      showFoldersHistoryPopup: false,
      showFolderShortcutsPopup: true,
    });
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} pluginUi
 * @param {FileListPluginUiProps} props
 * @param {FSPluginUiOptions} options
 */
function assertFSPluginUi(
  result,
  pluginUi,
  props,
  { showDrivePopupOnLeft, showFoldersHistoryPopup, showFolderShortcutsPopup }
) {
  assert.deepEqual(pluginUi.displayName, "FSPluginUi");

  assert.deepEqual(
    result.findByType(drive).props.onClose === props.onClose,
    true
  );

  const foldersHistoryProps = result.findByType(foldersHistory).props;
  const onChangeDirInActivePanel = foldersHistoryProps.onChangeDir;
  assert.deepEqual(foldersHistoryProps.onClose === props.onClose, true);

  const folderShortcutsProps = result.findByType(folderShortcuts).props;
  assert.deepEqual(folderShortcutsProps.onClose === props.onClose, true);
  assert.deepEqual(
    folderShortcutsProps.onChangeDir === onChangeDirInActivePanel,
    true
  );

  assertComponents(
    result.children,
    h(drive, {
      dispatch: props.dispatch,
      showDrivePopupOnLeft,
      onChangeDir: mockFunction(),
      onClose: props.onClose,
    }),

    h(foldersHistory, {
      showPopup: showFoldersHistoryPopup,
      onChangeDir: onChangeDirInActivePanel,
      onClose: props.onClose,
    }),

    h(folderShortcuts, {
      showPopup: showFolderShortcutsPopup,
      onChangeDir: onChangeDirInActivePanel,
      onClose: props.onClose,
    })
  );
}
