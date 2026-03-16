/**
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 */
import React from "react";
import { deepEqual, fail } from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import FSFileListActions from "../../fs/FSFileListActions.mjs";
import FSPlugin from "../../fs/FSPlugin.mjs";
import FSPluginLoader from "../../fs/FSPluginLoader.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fsComp = () => h("test stub");

describe("FSPlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["M-l", "M-r", "M-h", "C-d"];

    //when & then
    deepEqual(FSPluginLoader.triggerKeys, expected);
  });

  it("should initialize dispatch, actions and state when init", () => {
    //given
    const updatedState = FileListState();
    let reducerArgs = /** @type {any[]} */ ([]);
    const reducer = mockFunction((...args) => {
      reducerArgs = args;
      return updatedState;
    });
    const plugin = new FSPlugin(reducer);
    let parentDispatchArgs = /** @type {any[]} */ ([]);
    const parentDispatch = mockFunction(
      (...args) => (parentDispatchArgs = args),
    );
    const item = new PanelStackItem(plugin.component);
    /** @type {readonly PanelStackItem<any>[]} */
    let stackData = [item];
    const stack = new PanelStack(
      true,
      stackData,
      (f) => (stackData = f(stackData)),
    );

    //when
    plugin.init(parentDispatch, stack);

    //then
    const { component, dispatch, actions, state } = stackData[0];
    deepEqual(component === plugin.component, true);
    deepEqual(actions === FSFileListActions.instance, true);
    if (!dispatch) {
      fail("dispatch is undefined!");
    }
    if (!state) {
      fail("state is undefined!");
    }
    deepEqual(state, FileListState());

    //given
    const action = "test action";

    //when
    dispatch(action);

    //then
    deepEqual(reducer.times, 1);
    deepEqual(reducerArgs, [state, action]);
    deepEqual(parentDispatch.times, 1);
    deepEqual(parentDispatchArgs, [action]);
    const {
      component: resComponent,
      dispatch: resDispatch,
      actions: resActions,
      state: resState,
    } = stackData[0];
    deepEqual(resComponent === plugin.component, true);
    deepEqual(resDispatch === dispatch, true);
    deepEqual(resActions === actions, true);
    deepEqual(resState === updatedState, true);
  });

  it("should return undefined/value if non-/trigger key when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when & then
    deepEqual(await FSPluginLoader.onKeyTrigger("test_key", stacks), undefined);
    deepEqual(
      (await FSPluginLoader.onKeyTrigger("M-l", stacks)) !== undefined,
      true,
    );
  });

  it("should return ui options if trigger key when _createUiOptions", () => {
    //given
    const { _createUiOptions } = FSPlugin.instance;

    //when & then
    deepEqual(_createUiOptions("M-l"), {
      showDrivePopupOnLeft: true,
      showFoldersHistoryPopup: false,
      showFolderShortcutsPopup: false,
    });
    deepEqual(_createUiOptions("M-r"), {
      showDrivePopupOnLeft: false,
      showFoldersHistoryPopup: false,
      showFolderShortcutsPopup: false,
    });
    deepEqual(_createUiOptions("M-h"), {
      showDrivePopupOnLeft: undefined,
      showFoldersHistoryPopup: true,
      showFolderShortcutsPopup: false,
    });
    deepEqual(_createUiOptions("C-d"), {
      showDrivePopupOnLeft: undefined,
      showFoldersHistoryPopup: false,
      showFolderShortcutsPopup: true,
    });
    deepEqual(_createUiOptions("unknown"), undefined);
  });
});
