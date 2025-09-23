/**
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { FileListPanelProps } from "@farjs/filelist/FileListPanel.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListPanel from "@farjs/filelist/FileListPanel.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import MockFSService from "../../fs/MockFSService.mjs";
import FSFreeSpace from "../../fs/FSFreeSpace.mjs";
import FSFoldersHistory from "../../fs/FSFoldersHistory.mjs";
import FSPanel from "../../fs/FSPanel.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FSPanel.fileListPanelComp = mockComponent(FileListPanel);
FSPanel.fsFreeSpaceComp = mockComponent(FSFreeSpace);
FSPanel.fsFoldersHistory = mockComponent(FSFoldersHistory);

const { fileListPanelComp, fsFreeSpaceComp, fsFoldersHistory } = FSPanel;

describe("FSPanel.test.mjs", () => {
  it("should return false when onKeypress(unknown key)", () => {
    //given
    FSPanel.fsService = new MockFSService();
    const props = getFileListPanelProps();
    const comp = TestRenderer.create(h(FSPanel, props)).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when
    const result = panelProps.onKeypress(null, "unknown");

    //then
    assert.deepEqual(result, false);
  });

  it("should dispatch action when onKeypress(M-o)", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<void>} */
    const openItemP = Promise.resolve(undefined);
    let openItemArgs = /** @type {any[]} */ ([]);
    const openItem = mockFunction((...args) => {
      openItemArgs = args;
      return openItemP;
    });
    FSPanel.fsService = new MockFSService({ openItem });
    const props = getFileListPanelProps({
      dispatch,
      state: {
        ...FileListState(),
        currDir: FileListDir("/sub-dir", false, [FileListItem("item 1")]),
      },
    });
    const comp = TestRenderer.create(h(FSPanel, props)).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when
    const result = panelProps.onKeypress(null, "M-o");

    //then
    assert.deepEqual(result, true);
    assert.deepEqual(openItem.times, 1);
    assert.deepEqual(openItemArgs, ["/sub-dir", "item 1"]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Opening default app");
    await action.task.result;
  });

  it("should render initial component", () => {
    //given
    const props = getFileListPanelProps();

    //when
    const result = TestRenderer.create(h(FSPanel, props)).root;

    //then
    assertFSPanel(result, props);
  });
});

/**
 * @param {Partial<FileListPanelProps>} props
 * @returns {FileListPanelProps}
 */
function getFileListPanelProps(props = {}) {
  return {
    dispatch: mockFunction(),
    actions: new MockFileListActions(),
    state: FileListState(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {FileListPanelProps} props
 */
function assertFSPanel(result, props) {
  assert.deepEqual(FSPanel.displayName, "FSPanel");

  assertComponents(
    result.children,

    h(fileListPanelComp, {
      dispatch: props.dispatch,
      actions: props.actions,
      state: props.state,
    }),

    h(fsFreeSpaceComp, {
      dispatch: props.dispatch,
      currDir: props.state.currDir,
    }),

    h(fsFoldersHistory, {
      currDirPath: props.state.currDir.path,
    })
  );
}
