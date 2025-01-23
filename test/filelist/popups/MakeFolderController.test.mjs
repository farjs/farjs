import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import MakeFolderPopup from "../../../filelist/popups/MakeFolderPopup.mjs";
import MakeFolderController from "../../../filelist/popups/MakeFolderController.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import Task from "@farjs/ui/task/Task.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

MakeFolderController.makeFolderPopup = mockComponent(MakeFolderPopup);

const { makeFolderPopup } = MakeFolderController;

describe("MakeFolderController.test.mjs", () => {
  it("should dispatch action and save history when onOk", async () => {
    //given
    const dispatch = mockFunction();
    let createDirArgs = /** @type {any[]} */ ([]);
    const createDir = mockFunction((...args) => {
      createDirArgs = args;
      return action;
    });
    const actions = new MockFileListActions({ createDir });
    const currDir = FileListDir("/sub-dir", false, []);
    const state = { ...FileListState(), currDir };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showMkFolderPopup: true };

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
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(MakeFolderController, props), historyProvider)
    );
    const popupProps = renderer.root.findByType(makeFolderPopup).props;
    assert.deepEqual(popupProps.multiple, false);

    const dir = "test dir";
    const multiple = true;
    const action = TaskAction(
      Task(
        "Creating...",
        Promise.resolve(
          FileListDir("/sub-dir", false, [FileListItem(dir, true)])
        )
      )
    );

    //when
    popupProps.onOk(dir, multiple);

    //then
    await action.task.result;
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: dir }]);

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(createDir.times, 1);
    assert.deepEqual(createDirArgs, [dispatch, currDir.path, dir, multiple]);
  });

  it("should call onClose when onCancel", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const onClose = mockFunction();
    const props = { onClose, data, showMkFolderPopup: true };
    const historyProvider = new MockHistoryProvider();
    const comp = TestRenderer.create(
      withHistoryProvider(h(MakeFolderController, props), historyProvider)
    ).root;
    const popupProps = comp.findByType(makeFolderPopup).props;

    //when
    popupProps.onCancel();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data, showMkFolderPopup: true };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(MakeFolderController, props), historyProvider)
    ).root;

    //then
    assertMakeFolderController(result);
  });

  it("should render empty component when showMkFolderPopup is undefined", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(MakeFolderController, props), historyProvider)
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });

  it("should render empty component when data is undefined", () => {
    //given
    const props = { onClose: mockFunction(), showMkFolderPopup: true };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(MakeFolderController, props), historyProvider)
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertMakeFolderController(result) {
  assert.deepEqual(MakeFolderController.displayName, "MakeFolderController");

  assertComponents(
    result.children,
    h(makeFolderPopup, {
      multiple: true,
      onOk: mockFunction(),
      onCancel: mockFunction(),
    })
  );
}
