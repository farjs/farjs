/**
 * @typedef {import("../../file/FileViewHistory.mjs").FileViewHistoryParams} FileViewHistoryParams
 */
import fs from "fs";
import path from "path";
import React from "react";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import FileEvent from "../../file/FileEvent.mjs";
import FileViewHistory from "../../file/FileViewHistory.mjs";
import ViewerEvent from "../../viewer/ViewerEvent.mjs";
import ViewerPlugin from "../../viewer/ViewerPlugin.mjs";

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
/** @type {FileViewHistoryParams} */
const params = {
  isEdit: false,
  encoding: "utf8",
  position: 123,
};
const data = FileViewHistory("test/path", params);

describe("ViewerPlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = [
      "f3",
      "onViewerOpenLeft",
      "onViewerOpenRight",
      "onFileView",
    ];

    //when & then
    assert.deepEqual(ViewerPlugin.triggerKeys, expected);
  });

  it("should return rejected Promise if no such file when onKeyTrigger(onFileView)", async () => {
    //given
    const error = Error("no such file");
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      throw error;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const leftState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );
    let capturedError = null;

    //when
    try {
      await ViewerPlugin.onKeyTrigger(FileEvent.onFileView, stacks, data);
    } catch (err) {
      capturedError = err;
    }

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    assert.deepEqual(capturedError, error);
    assert.deepEqual(lstatSyncArgs, [data.path]);
  });

  it("should return ViewerPluginUi when onKeyTrigger(onFileView)", async () => {
    //given
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      return { size: 50 };
    });
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
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger(
      FileEvent.onFileView,
      stacks,
      data
    );

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    assert.deepEqual(result?.displayName, "ViewerPluginUi");
    assert.deepEqual(lstatSyncArgs, [data.path]);
  });

  it("should return undefined if data is undefined when onKeyTrigger(onFileView)", async () => {
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
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger(
      FileEvent.onFileView,
      stacks
    );

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return undefined if no state data when onKeyTrigger(f3)", async () => {
    //given
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return undefined if .. when onKeyTrigger(f3)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("item 1")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return undefined if non-local fs when onKeyTrigger(f3)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
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
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return rejected Promise if no such file when onKeyTrigger(f3)", async () => {
    //given
    const error = Error("no such file");
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      throw error;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 1");
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [item],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );
    let capturedError = null;

    //when
    try {
      await ViewerPlugin.onKeyTrigger("f3", stacks);
    } catch (err) {
      capturedError = err;
    }

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    const filePath = path.join(leftState.currDir.path, item.name);
    assert.deepEqual(capturedError, error);
    assert.deepEqual(lstatSyncArgs, [filePath]);
  });

  it("should return ViewerPluginUi if file when onKeyTrigger(f3)", async () => {
    //given
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      return { size: 50 };
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 1");
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [item],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    const filePath = path.join(leftState.currDir.path, item.name);
    assert.deepEqual(result?.displayName, "ViewerPluginUi");
    assert.deepEqual(lstatSyncArgs, [filePath]);
  });

  it("should return ViewerPluginUi if file when onKeyTrigger(onViewerOpenLeft)", async () => {
    //given
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      return { size: 50 };
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 1");
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [item],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger(
      ViewerEvent.onViewerOpenLeft,
      stacks
    );

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    const filePath = path.join(leftState.currDir.path, item.name);
    assert.deepEqual(result?.displayName, "ViewerPluginUi");
    assert.deepEqual(lstatSyncArgs, [filePath]);
  });

  it("should return ViewerPluginUi if file when onKeyTrigger(onViewerOpenRight)", async () => {
    //given
    const savedLstasSync = fs.lstatSync;
    let lstatSyncArgs = /** @type {any[]} */ ([]);
    //@ts-ignore
    fs.lstatSync = mockFunction((...args) => {
      lstatSyncArgs = args;
      return { size: 50 };
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 1");
    const rightState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [item],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, rightState)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger(
      ViewerEvent.onViewerOpenRight,
      stacks
    );

    //@ts-ignore
    fs.lstatSync = savedLstasSync;

    //then
    const filePath = path.join(rightState.currDir.path, item.name);
    assert.deepEqual(result?.displayName, "ViewerPluginUi");
    assert.deepEqual(lstatSyncArgs, [filePath]);
  });

  it("should return ViewItemsPopup if selected items when onKeyTrigger(f3)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("dir 1", true)],
      },
      selectedNames: new Set(["dir 1"]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //then
    assert.deepEqual(result?.displayName, "ViewItemsPopup");
  });

  it("should return ViewItemsPopup if dir when onKeyTrigger(f3)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("dir 1", true)],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await ViewerPlugin.onKeyTrigger("f3", stacks);

    //then
    assert.deepEqual(result?.displayName, "ViewItemsPopup");
  });
});
