import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import DeleteController from "../../../filelist/popups/DeleteController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

DeleteController.messageBoxComp = mockComponent(MessageBox);

const { messageBoxComp } = DeleteController;

describe("DeleteController.test.mjs", () => {
  it("should call api and delete currItem when YES action", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let deleteItemsArgs = /** @type {any[]} */ ([]);
    const deleteItems = mockFunction((...args) => {
      deleteItemsArgs = args;
      return action;
    });
    const actions = new MockFileListActions({ deleteItems });
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem("file 1"),
      FileListItem("file 2"),
    ]);
    const state = { ...FileListState(), currDir };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showDeletePopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(DeleteController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;
    const action = TaskAction(Task("Deleting Items", Promise.resolve()));
    const items = [FileListItem("file 1")];

    //when
    msgBoxProps.actions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
    assert.deepEqual(deleteItems.times, 1);
    assert.deepEqual(deleteItemsArgs, [dispatch, currDir.path, items]);

    await action.task.result;
  });

  it("should call api and delete selectedItems when YES action", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let deleteItemsArgs = /** @type {any[]} */ ([]);
    const deleteItems = mockFunction((...args) => {
      deleteItemsArgs = args;
      return action;
    });
    const actions = new MockFileListActions({ deleteItems });
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem("file 1"),
      FileListItem("file 2"),
    ]);
    const state = {
      ...FileListState(),
      currDir,
      selectedNames: new Set(["file 2"]),
    };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showDeletePopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(DeleteController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;
    const action = TaskAction(Task("Deleting Items", Promise.resolve()));
    const items = [FileListItem("file 2")];

    //when
    msgBoxProps.actions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
    assert.deepEqual(deleteItems.times, 1);
    assert.deepEqual(deleteItemsArgs, [dispatch, currDir.path, items]);

    await action.task.result;
  });

  it("should call onClose when NO action", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const onClose = mockFunction();
    const props = { onClose, data, showDeletePopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(DeleteController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;

    //when
    msgBoxProps.actions[1].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data, showDeletePopup: true };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(DeleteController, props))
    ).root;

    //then
    assertDeleteController(result);
  });

  it("should render empty component", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(DeleteController, props))
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertDeleteController(result) {
  assert.deepEqual(DeleteController.displayName, "DeleteController");

  const currTheme = FileListTheme.defaultTheme;

  assertComponents(
    result.children,
    h(messageBoxComp, {
      title: "Delete",
      message: "Do you really want to delete selected item(s)?",
      actions: [
        MessageBoxAction.YES(mockFunction()),
        MessageBoxAction.NO(mockFunction()),
      ],
      style: currTheme.popup.error,
    })
  );
}
