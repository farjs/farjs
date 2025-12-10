/**
 * @template T
 * @typedef {import("@farjs/ui/task/TaskAction.mjs").TaskAction<T>} TaskAction
 */
/**
 * @import { FileListParamsChangedAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { AddToArchControllerProps } from "../../archiver/AddToArchController.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import AddToArchPopup from "../../archiver/AddToArchPopup.mjs";
import AddToArchController from "../../archiver/AddToArchController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

AddToArchController.addToArchPopup = mockComponent(AddToArchPopup);
AddToArchController.statusPopupComp = mockComponent(StatusPopup);

const { addToArchPopup, statusPopupComp } = AddToArchController;

describe("AddToArchController.test.mjs", () => {
  it("should call onCancel when onCancel in modal", () => {
    //given
    const onCancel = mockFunction();
    const props = getAddToArchControllerProps({ onCancel });
    const comp = TestRenderer.create(h(AddToArchController, props)).root;
    const popupProps = comp.findByType(addToArchPopup).props;

    //when
    popupProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should dispatch failed task action when failed", async () => {
    //given
    const onComplete = mockFunction();
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return Promise.reject(Error("test error"));
    });
    const actions = new MockFileListActions({ scanDirs });
    const items = [FileListItem("dir 3", true)];
    const props = getAddToArchControllerProps({
      dispatch,
      actions,
      state: {
        ...FileListState(),
        index: 1,
        currDir: {
          path: "/sub-dir",
          isRoot: false,
          items: [
            FileListItem.up,
            FileListItem("item 1"),
            FileListItem("item 2"),
            ...items,
          ],
        },
        selectedNames: new Set(["dir 3"]),
      },
      archName: "new.zip",
      archType: "zip",
      items,
      onComplete,
    });
    const renderer = TestRenderer.create(h(AddToArchController, props));
    const popupProps = renderer.root.findByType(addToArchPopup).props;
    const zipFile = "test.zip";

    //when
    await actAsync(() => {
      popupProps.onAction(zipFile);
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(addToArchPopup), []);
    assert.deepEqual(scanDirs.times, 1);
    assert.deepEqual(scanDirsArgs.slice(0, 2), [
      props.state.currDir.path,
      items,
    ]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Add item(s) to zip archive");
    assert.deepEqual(onComplete.times, 0);
  });

  it("should render status popup with progress and publish FileListParamsChangedAction", async () => {
    //given
    let onCompleteArgs = /** @type {any[]} */ ([]);
    const onComplete = mockFunction((...args) => (onCompleteArgs = args));
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return Promise.resolve(false);
    });
    let addToArchApiArgs = /** @type {any[]} */ ([]);
    const addToArchApi = mockFunction((...args) => {
      addToArchApiArgs = args;
      return addToArchApiP;
    });
    /** @type {any} */
    let addToArchApiResolve;
    /** @type {Promise<void>} */
    const addToArchApiP = new Promise(
      (resolve) => (addToArchApiResolve = resolve)
    );
    const actions = new MockFileListActions({ scanDirs });
    const items = [FileListItem("dir 3", true)];
    const props = getAddToArchControllerProps({
      dispatch,
      actions,
      state: {
        ...FileListState(),
        index: 1,
        currDir: {
          path: "/sub-dir",
          isRoot: false,
          items: [
            FileListItem.up,
            FileListItem("item 1"),
            FileListItem("item 2"),
            ...items,
          ],
        },
        selectedNames: new Set(["dir 3"]),
      },
      archName: "new.zip",
      archType: "zip",
      addToArchApi,
      items,
      onComplete,
    });
    const renderer = TestRenderer.create(h(AddToArchController, props));
    const popupProps = renderer.root.findByType(addToArchPopup).props;
    const zipFile = "test.zip";

    //when
    await actAsync(() => {
      popupProps.onAction(zipFile);
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(addToArchPopup), []);
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n0%",
    });
    assert.deepEqual(scanDirs.times, 1);
    const onNextDir = scanDirsArgs[2];
    assert.deepEqual(scanDirsArgs, [
      props.state.currDir.path,
      items,
      onNextDir,
    ]);
    onNextDir("/path", [
      FileListItem("dir 2", true),
      { ...FileListItem("file 1"), size: 123 },
    ]);
    assert.deepEqual(addToArchApi.times, 1);
    const onNextItem = addToArchApiArgs[3];
    assert.deepEqual(addToArchApiArgs, [
      zipFile,
      props.state.currDir.path,
      new Set(["dir 3"]),
      onNextItem,
    ]);

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n33%",
    });

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n66%",
    });

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n100%",
    });

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n100%",
    });

    //when
    await actAsync(() => {
      addToArchApiResolve();
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(dispatch.times, 1);
    /** @type {FileListParamsChangedAction} */
    const action = {
      action: "FileListParamsChangedAction",
      offset: 0,
      index: 1,
      selectedNames: new Set(),
    };
    assert.deepEqual(dispatchArgs, [action]);
    assert.deepEqual(onComplete.times, 1);
    assert.deepEqual(onCompleteArgs, [zipFile]);
  });

  it("should render status popup with progress and not publish FileListParamsChangedAction", async () => {
    //given
    let onCompleteArgs = /** @type {any[]} */ ([]);
    const onComplete = mockFunction((...args) => (onCompleteArgs = args));
    const dispatch = mockFunction();
    let scanDirsArgs = /** @type {any[]} */ ([]);
    const scanDirs = mockFunction((...args) => {
      scanDirsArgs = args;
      return Promise.resolve(false);
    });
    let addToArchApiArgs = /** @type {any[]} */ ([]);
    const addToArchApi = mockFunction((...args) => {
      addToArchApiArgs = args;
      return addToArchApiP;
    });
    /** @type {any} */
    let addToArchApiResolve;
    /** @type {Promise<void>} */
    const addToArchApiP = new Promise(
      (resolve) => (addToArchApiResolve = resolve)
    );
    const actions = new MockFileListActions({ scanDirs });
    const items = [FileListItem("item 2")];
    const props = getAddToArchControllerProps({
      dispatch,
      actions,
      state: {
        ...FileListState(),
        index: 2,
        currDir: {
          path: "/sub-dir",
          isRoot: false,
          items: [FileListItem.up, FileListItem("item 1"), ...items],
        },
      },
      archName: "new.zip",
      archType: "zip",
      addToArchApi,
      items,
      onComplete,
    });
    const renderer = TestRenderer.create(h(AddToArchController, props));
    const popupProps = renderer.root.findByType(addToArchPopup).props;
    const zipFile = "test.zip";

    //when
    await actAsync(() => {
      popupProps.onAction(zipFile);
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(addToArchPopup), []);
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n0%",
    });
    assert.deepEqual(scanDirs.times, 1);
    const onNextDir = scanDirsArgs[2];
    assert.deepEqual(scanDirsArgs, [
      props.state.currDir.path,
      items,
      onNextDir,
    ]);
    assert.deepEqual(addToArchApi.times, 1);
    const onNextItem = addToArchApiArgs[3];
    assert.deepEqual(addToArchApiArgs, [
      zipFile,
      props.state.currDir.path,
      new Set(["item 2"]),
      onNextItem,
    ]);

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n100%",
    });

    //when & then
    await actAsync(() => {
      onNextItem();
    });
    assert.deepEqual(renderer.root.findByType(statusPopupComp).props, {
      text: "Add item(s) to zip archive\n100%",
    });

    //when
    await actAsync(() => {
      addToArchApiResolve();
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(statusPopupComp), []);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(onComplete.times, 1);
    assert.deepEqual(onCompleteArgs, [zipFile]);
  });

  it("should render initial component", () => {
    //given
    const onCancel = mockFunction();
    const props = getAddToArchControllerProps({ onCancel });

    //when
    const result = TestRenderer.create(h(AddToArchController, props)).root;

    //then
    assertAddToArchController(result, props);
  });
});

/**
 * @param {Partial<AddToArchControllerProps>} props
 * @returns {AddToArchControllerProps}
 */
function getAddToArchControllerProps(props = {}) {
  return {
    dispatch: mockFunction(),
    actions: new MockFileListActions(),
    state: FileListState(),
    archName: "test archName",
    archType: "arch",
    archAction: "Add",
    addToArchApi: mockFunction(),
    items: [],
    onComplete: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {AddToArchControllerProps} props
 */
function assertAddToArchController(result, props) {
  assert.deepEqual(AddToArchController.displayName, "AddToArchController");

  assertComponents(
    result.children,
    h(addToArchPopup, {
      archName: props.archName,
      archType: props.archType,
      action: props.archAction,
      onAction: mockFunction(),
      onCancel: mockFunction(),
    })
  );
}
