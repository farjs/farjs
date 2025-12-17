/**
 * @import { FileListDir } from "@farjs/filelist/api/FileListDir.mjs")
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs"
 * @import { ArchiverPluginUiParams } from "../../archiver/ArchiverPluginUi.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import AddToArchController from "../../archiver/AddToArchController.mjs";
import ArchiverPluginUi from "../../archiver/ArchiverPluginUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ArchiverPluginUi.addToArchController = mockComponent(AddToArchController);

const { addToArchController } = ArchiverPluginUi;

describe("ArchiverPluginUi.test.mjs", () => {
  it("should dispatch actions when onComplete", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return updateAction;
    });
    const props = { dispatch, onClose };
    const items = [FileListItem("item 2"), FileListItem("item 3")];
    const data = FileListData(
      dispatch,
      new MockFileListActions({ updateDir }),
      {
        ...FileListState(),
        index: 1,
        currDir: {
          path: "/sub-dir",
          isRoot: false,
          items: [FileListItem.up, FileListItem("item 1"), ...items],
        },
        selectedNames: new Set(["item 3", "item 2"]),
      }
    );
    const params = getArchiverPluginUiParams({ data, items });
    const pluginUi = ArchiverPluginUi(params);
    const comp = TestRenderer.create(h(pluginUi, props)).root;
    const controller = comp.findByType(addToArchController).props;
    const zipFile = "test.zip";
    /** @type {FileListDir} */
    const updatedDir = {
      path: "/updated/dir",
      isRoot: false,
      items: [FileListItem("file 1")],
    };
    const updateAction = TaskAction(
      Task("Updating...", Promise.resolve(updatedDir))
    );

    //when
    controller.onComplete(zipFile);

    //then
    const taskRes = await updateAction.task.result;
    assert.deepEqual(taskRes === updatedDir, true);
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(updateDir.times, 1);
    assert.deepEqual(updateDirArgs, [data.dispatch, data.state.currDir.path]);
    assert.deepEqual(dispatch.times, 2);
    /** @type {FileListAction} */
    const action2 = {
      action: "FileListItemCreatedAction",
      name: zipFile,
      currDir: updatedDir,
    };
    assert.deepEqual(dispatchArgs, [updateAction, action2]);
  });

  it("should render component", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(
      dispatch,
      new MockFileListActions(),
      FileListState()
    );
    const params = getArchiverPluginUiParams({ data });
    const pluginUi = ArchiverPluginUi(params);

    //when
    const result = TestRenderer.create(h(pluginUi, props)).root;

    //then
    assertArchiverPluginUi(result, pluginUi, props, params);
  });
});

/**
 * @param {Partial<ArchiverPluginUiParams>} props
 * @returns {ArchiverPluginUiParams}
 */
function getArchiverPluginUiParams(props = {}) {
  return {
    data: {
      dispatch: mockFunction(),
      actions: new MockFileListActions(),
      state: FileListState(),
    },
    archName: "test archName",
    archType: "arch",
    addToArchApi: mockFunction(),
    items: [],
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} pluginUi
 * @param {FileListPluginUiProps} props
 * @param {ArchiverPluginUiParams} params
 */
function assertArchiverPluginUi(result, pluginUi, props, params) {
  assert.deepEqual(pluginUi.displayName, "ArchiverPluginUi");

  const controller = result.findByType(addToArchController).props;
  assert.deepEqual(controller.addToArchApi === params.addToArchApi, true);
  assert.deepEqual(controller.onCancel === props.onClose, true);

  assertComponents(
    result.children,
    h(addToArchController, {
      dispatch: params.data.dispatch,
      actions: params.data.actions,
      state: params.data.state,
      archName: params.archName,
      archType: params.archType,
      archAction: "Add",
      addToArchApi: params.addToArchApi,
      items: params.items,
      onComplete: mockFunction(),
      onCancel: mockFunction(),
    })
  );
}
