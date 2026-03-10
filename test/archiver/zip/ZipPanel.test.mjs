/**
 * @template T
 * @typedef {import("@farjs/ui/task/TaskAction.mjs").TaskAction<T>} TaskAction
 * @import { MessageBoxProps } from "@farjs/ui/popup/MessageBox.mjs"
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPanelProps } from "@farjs/filelist/FileListPanel.mjs"
 * @import {
 *  FileListDiskSpaceUpdatedAction,
 *  FileListDirChangedAction,
 * } from "@farjs/filelist/FileListActions.mjs"
 * @import { AddToArchControllerProps } from "../../../archiver/AddToArchController.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import { deepEqual } from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import FileListEvent from "@farjs/filelist/FileListEvent.mjs";
import FileListPanel from "@farjs/filelist/FileListPanel.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import AddToArchController from "../../../archiver/AddToArchController.mjs";
import ZipEntry from "../../../archiver/zip/ZipEntry.mjs";
import ZipPanel from "../../../archiver/zip/ZipPanel.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ZipPanel.fileListPanelComp = mockComponent(FileListPanel);
ZipPanel.addToArchController = mockComponent(AddToArchController);
ZipPanel.messageBoxComp = mockComponent(MessageBox);

const { fileListPanelComp, addToArchController, messageBoxComp } = ZipPanel;

const fsComp = () => null;
const zipComp = () => null;
const entriesByParentP = Promise.resolve(
  new Map([
    [
      "",
      [ZipEntry("", "dir 1", true, 0, 1), ZipEntry("", "file 1", false, 2, 3)],
    ],
    ["dir 1", [ZipEntry("dir 1", "dir 2", true, 0, 4)]],
    ["dir 1/dir 2", [ZipEntry("dir 1/dir 2", "file 2", false, 5, 6)]],
  ]),
);

describe("ZipPanel.test.mjs", () => {
  it("should return false when onKeypress(unknown)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        currDir: FileListDir("zip://filePath.zip", false, [FileListItem.up]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when & then
    deepEqual(panelProps.onKeypress(null, "unknown"), false);
  });

  it("should call onClose if root dir when onKeypress(C-pageup)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("item 1"),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return updateAction;
    });
    let fsDispatchArgs = /** @type {any[]} */ ([]);
    const fsDispatch = mockFunction((...args) => fsDispatchArgs.push(...args));
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: true }),
      updateDir,
    });
    const fsState = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, []),
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      true,
      [
        new PanelStackItem(zipComp),
        new PanelStackItem(fsComp, fsDispatch, fsActions, fsState),
      ],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const updateAction = TaskAction(
      Task("Updating...", Promise.resolve(updatedDir)),
    );

    //when & then
    deepEqual(panelProps.onKeypress(null, "C-pageup"), true);

    //then
    deepEqual(onClose.times, 1);
    deepEqual(updateDir.times, 1);
    deepEqual(updateDirArgs, [fsDispatch, fsState.currDir.path]);
    deepEqual(fsDispatch.times, 1);
    deepEqual(fsDispatchArgs, [updateAction]);
  });

  it("should not call onClose if not root dir when onKeypress(C-pageup)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip/sub-dir", false, [
          FileListItem.up,
          FileListItem("item 1"),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: true }),
    });
    const fsState = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, []),
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      true,
      [
        new PanelStackItem(zipComp),
        new PanelStackItem(fsComp, fsDispatch, fsActions, fsState),
      ],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when & then
    deepEqual(panelProps.onKeypress(null, "C-pageup"), false);
    deepEqual(onClose.times, 0);
  });

  it("should call onClose if on .. in root dir when onKeypress(enter|C-pagedown)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("item 1"),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return updateAction;
    });
    let fsDispatchArgs = /** @type {any[]} */ ([]);
    const fsDispatch = mockFunction((...args) => fsDispatchArgs.push(...args));
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: true }),
      updateDir,
    });
    const fsState = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, []),
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      true,
      [
        new PanelStackItem(zipComp),
        new PanelStackItem(fsComp, fsDispatch, fsActions, fsState),
      ],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const updateAction = TaskAction(
      Task("Updating...", Promise.resolve(updatedDir)),
    );

    //when & then
    deepEqual(panelProps.onKeypress(null, "enter"), true);
    deepEqual(onClose.times, 1);
    deepEqual(updateDir.times, 1);
    deepEqual(updateDirArgs, [fsDispatch, fsState.currDir.path]);
    deepEqual(fsDispatch.times, 1);
    deepEqual(fsDispatchArgs, [updateAction]);

    //when & then
    deepEqual(panelProps.onKeypress(null, "C-pagedown"), true);
    deepEqual(onClose.times, 2);
    deepEqual(updateDir.times, 2);
    deepEqual(updateDirArgs, [fsDispatch, fsState.currDir.path]);
    deepEqual(fsDispatch.times, 2);
    deepEqual(fsDispatchArgs, [updateAction, updateAction]);
  });

  it("should not call onClose if on .. not in root dir when onKeypress(enter|C-pagedown)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        currDir: FileListDir("zip://filePath.zip/sub-dir", false, [
          FileListItem.up,
          FileListItem("item 1"),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: true }),
    });
    const fsState = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, []),
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      true,
      [
        new PanelStackItem(zipComp),
        new PanelStackItem(fsComp, fsDispatch, fsActions, fsState),
      ],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when & then
    deepEqual(panelProps.onKeypress(null, "enter"), false);
    deepEqual(onClose.times, 0);
  });

  it("should not render AddToArchController if non-local FS when onKeypress(onFileListCopy)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
    const fsState = {
      ...FileListState(),
      index: 1,
      currDir: FileListDir("/sub-dir", false, [
        FileListItem.up,
        FileListItem("file 1"),
      ]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when & then
    deepEqual(panelProps.onKeypress(null, FileListEvent.onFileListCopy), false);

    //then
    deepEqual(comp.findAllByType(addToArchController), []);
  });

  it("should not render AddToArchController if .. when onKeypress(onFileListCopy)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
    const fsState = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, [
        FileListItem.up,
        FileListItem("file 1"),
      ]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const comp = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;
    const panelProps = comp.findByType(fileListPanelComp).props;

    //when & then
    deepEqual(panelProps.onKeypress(null, FileListEvent.onFileListCopy), false);

    //then
    deepEqual(comp.findAllByType(addToArchController), []);
  });

  it("should render AddToArchController and handle onCancel when onKeypress(onFileListCopy)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const items = [FileListItem("file 1")];
    const fsState = {
      ...FileListState(),
      index: 1,
      currDir: FileListDir("/sub-dir", false, [FileListItem.up, ...items]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const renderer = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    );
    const panelProps = renderer.root.findByType(fileListPanelComp).props;

    //when & then
    TestRenderer.act(() => {
      deepEqual(
        panelProps.onKeypress(null, FileListEvent.onFileListCopy),
        true,
      );
    });
    /** @type {AddToArchControllerProps} */
    const controllerProps = /** @type {any} */ (
      renderer.root.findByType(addToArchController).props
    );
    deepEqual(
      controllerProps,
      /** @type {AddToArchControllerProps} */ ({
        dispatch: fsDispatch,
        actions: fsActions,
        state: fsState,
        archName: "dir/file.zip",
        archType: "zip",
        archAction: "Copy",
        addToArchApi: controllerProps.addToArchApi,
        items,
        onComplete: controllerProps.onComplete,
        onCancel: controllerProps.onCancel,
      }),
    );

    //when & then
    TestRenderer.act(() => {
      controllerProps.onCancel();
    });
    deepEqual(renderer.root.findAllByType(addToArchController), []);
  });

  it("should render AddToArchController and handle onComplete when onKeypress(onFileListCopy)", () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return updateAction;
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
      updateDir,
    });
    const props = getFileListPanelProps({
      dispatch,
      actions,
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const items = [FileListItem("item 2"), FileListItem("item 3")];
    const fsState = {
      ...FileListState(),
      index: 1,
      currDir: FileListDir("/sub-dir", false, [
        FileListItem.up,
        FileListItem("item 1"),
        ...items,
      ]),
      selectedNames: new Set(["item 3", "item 2"]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const renderer = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    );
    const panelProps = renderer.root.findByType(fileListPanelComp).props;

    //when & then
    TestRenderer.act(() => {
      deepEqual(
        panelProps.onKeypress(null, FileListEvent.onFileListCopy),
        true,
      );
    });
    /** @type {AddToArchControllerProps} */
    const controllerProps = /** @type {any} */ (
      renderer.root.findByType(addToArchController).props
    );
    deepEqual(
      controllerProps,
      /** @type {AddToArchControllerProps} */ ({
        dispatch: fsDispatch,
        actions: fsActions,
        state: fsState,
        archName: "dir/file.zip",
        archType: "zip",
        archAction: "Copy",
        addToArchApi: controllerProps.addToArchApi,
        items,
        onComplete: controllerProps.onComplete,
        onCancel: controllerProps.onCancel,
      }),
    );

    //given
    const zipFile = "test.zip";
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const updateAction = TaskAction(
      Task("Updating...", Promise.resolve(updatedDir)),
    );

    //when & then
    TestRenderer.act(() => {
      controllerProps.onComplete(zipFile);
    });
    deepEqual(renderer.root.findAllByType(addToArchController), []);
    deepEqual(dispatch.times, 1);
    deepEqual(dispatchArgs, [updateAction]);
    deepEqual(updateDir.times, 1);
    deepEqual(updateDirArgs, [dispatch, props.state.currDir.path]);
  });

  it("should render AddToArchController and handle onComplete when onKeypress(onFileListMove)", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return updateAction;
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
      updateDir,
    });
    const props = getFileListPanelProps({
      dispatch,
      actions,
      state: {
        ...FileListState(),
        index: 1,
        currDir: FileListDir("zip://filePath.zip", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    let deleteItemsArgs = /** @type {any[]} */ ([]);
    const deleteItems = mockFunction((...args) => {
      deleteItemsArgs = args;
      return deleteAction;
    });
    let fsDispatchArgs = /** @type {any[]} */ ([]);
    const fsDispatch = mockFunction((...args) => fsDispatchArgs.push(...args));
    const fsActions = new MockFileListActions({ deleteItems });
    const items = [FileListItem("item 2"), FileListItem("item 3")];
    const fsState = {
      ...FileListState(),
      index: 1,
      currDir: FileListDir("/sub-dir", false, [
        FileListItem.up,
        FileListItem("item 1"),
        ...items,
      ]),
      selectedNames: new Set(["item 3", "item 2"]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const renderer = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    );
    const panelProps = renderer.root.findByType(fileListPanelComp).props;

    //when & then
    TestRenderer.act(() => {
      deepEqual(
        panelProps.onKeypress(null, FileListEvent.onFileListMove),
        true,
      );
    });
    /** @type {AddToArchControllerProps} */
    const controllerProps = /** @type {any} */ (
      renderer.root.findByType(addToArchController).props
    );
    deepEqual(
      controllerProps,
      /** @type {AddToArchControllerProps} */ ({
        dispatch: fsDispatch,
        actions: fsActions,
        state: fsState,
        archName: "dir/file.zip",
        archType: "zip",
        archAction: "Move",
        addToArchApi: controllerProps.addToArchApi,
        items,
        onComplete: controllerProps.onComplete,
        onCancel: controllerProps.onCancel,
      }),
    );

    //given
    const zipFile = "test.zip";
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const updateAction = TaskAction(
      Task("Updating...", Promise.resolve(updatedDir)),
    );
    /** @type {TaskAction<any>} */
    const deleteAction = TaskAction(Task("Deleting...", Promise.resolve()));

    //when & then
    TestRenderer.act(() => {
      controllerProps.onComplete(zipFile);
    });
    deepEqual(renderer.root.findAllByType(addToArchController), []);
    deepEqual(dispatch.times, 1);
    deepEqual(dispatchArgs, [updateAction]);
    deepEqual(updateDir.times, 1);
    deepEqual(updateDirArgs, [dispatch, props.state.currDir.path]);
    await Promise.resolve();
    deepEqual(fsDispatch.times, 1);
    deepEqual(fsDispatchArgs, [deleteAction]);
    deepEqual(deleteItems.times, 1);
    deepEqual(deleteItemsArgs, [fsDispatch, fsState.currDir.path, items]);
  });

  it("should render MessageBox if non-root dir when onKeypress(onFileListCopy|Move)", () => {
    //given
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      state: {
        ...FileListState(),
        currDir: FileListDir("zip://filePath.zip/sub-dir", false, [
          FileListItem.up,
          FileListItem("dir 1", true),
        ]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = {
      ...FileListState(),
      index: 1,
      currDir: FileListDir("/sub-dir", false, [
        FileListItem.up,
        FileListItem("item 1"),
      ]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );
    const renderer = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    );
    const panelProps = renderer.root.findByType(fileListPanelComp).props;

    //when & then
    TestRenderer.act(() => {
      deepEqual(
        panelProps.onKeypress(null, FileListEvent.onFileListCopy),
        true,
      );
    });
    deepEqual(renderer.root.findAllByType(addToArchController), []);
    /** @type {MessageBoxProps} */
    const msgBoxProps = /** @type {any} */ (
      renderer.root.findByType(messageBoxComp).props
    );
    const onOkAction = msgBoxProps.actions[0].onAction;
    deepEqual(
      msgBoxProps,
      /** @type {MessageBoxProps} */ ({
        title: "Warning",
        message: "Items can only be added to zip root.",
        actions: [MessageBoxAction.OK(onOkAction)],
        style: DefaultTheme.popup.regular,
      }),
    );

    //when & then
    TestRenderer.act(() => {
      onOkAction();
    });
    deepEqual(renderer.root.findAllByType(messageBoxComp), []);
  });

  it("should dispatch action with empty dir if rejected entries Promise", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      dispatch,
      state: FileListState(),
    });
    const rootPath = "zip://filePath.zip";
    const error = Error("test");
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      Promise.reject(error),
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );

    //when
    const result = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;

    //then
    assertZipPanel(result, zipPanel, props);
    deepEqual(onClose.times, 0);
    deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const taskAction = dispatchArgs[0];
    deepEqual(taskAction.task.message, "Reading zip archive");
    let capturedError = null;
    try {
      await taskAction.task.result;
    } catch (err) {
      capturedError = err;
    }
    deepEqual(capturedError, error);

    deepEqual(dispatch.times, 2);
    /** @type {FileListDirChangedAction} */
    const dirUpdatedAction = {
      action: "FileListDirChangedAction",
      dir: FileListItem.currDir.name,
      currDir: FileListDir(rootPath, false, []),
    };
    deepEqual(dispatchArgs[1], dirUpdatedAction);
  });

  it("should dispatch actions with empty root dir if empty state", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      dispatch,
      state: FileListState(),
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      Promise.resolve(new Map()),
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );

    //when
    const result = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;

    //then
    assertZipPanel(result, zipPanel, props);
    deepEqual(onClose.times, 0);
    deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const taskAction = dispatchArgs[0];
    deepEqual(taskAction.task.message, "Reading zip archive");
    await taskAction.task.result;

    deepEqual(dispatch.times, 3);
    /** @type {FileListDiskSpaceUpdatedAction} */
    const diskSpaceUpdatedAction = {
      action: "FileListDiskSpaceUpdatedAction",
      diskSpace: 0,
    };
    deepEqual(dispatchArgs[1], diskSpaceUpdatedAction);
    /** @type {FileListDirChangedAction} */
    const dirUpdatedAction = {
      action: "FileListDirChangedAction",
      dir: FileListItem.currDir.name,
      currDir: FileListDir(rootPath, false, []),
    };
    deepEqual(dispatchArgs[2], dirUpdatedAction);
  });

  it("should not dispatch actions if state is not empty when mount", async () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      dispatch,
      state: {
        ...FileListState(),
        currDir: FileListDir("zip://filePath.zip", false, [FileListItem.up]),
      },
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      Promise.resolve(new Map()),
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );

    //when
    const result = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;

    //then
    assertZipPanel(result, zipPanel, props);
    await Promise.resolve();
    await Promise.resolve();
    deepEqual(onClose.times, 0);
    deepEqual(dispatch.times, 0);
  });

  it("should render initial component and dispatch actions", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(...args));
    const onClose = mockFunction();
    const props = getFileListPanelProps({
      dispatch,
      state: FileListState(),
    });
    const rootPath = "zip://filePath.zip";
    const zipPanel = ZipPanel(
      "dir/file.zip",
      rootPath,
      entriesByParentP,
      onClose,
    );
    const fsDispatch = mockFunction();
    const fsActions = new MockFileListActions();
    const fsState = FileListState();
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, fsDispatch, fsActions, fsState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(zipComp)],
      mockFunction(),
    );

    //when
    const result = TestRenderer.create(
      withStacksContext(withThemeContext(h(zipPanel, props)), {
        left: WithStacksData(leftStack),
        right: WithStacksData(rightStack),
      }),
    ).root;

    //then
    assertZipPanel(result, zipPanel, props);
    deepEqual(onClose.times, 0);
    deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const taskAction = dispatchArgs[0];
    deepEqual(taskAction.task.message, "Reading zip archive");
    await taskAction.task.result;

    deepEqual(dispatch.times, 3);
    /** @type {FileListDiskSpaceUpdatedAction} */
    const diskSpaceUpdatedAction = {
      action: "FileListDiskSpaceUpdatedAction",
      diskSpace: 7,
    };
    deepEqual(dispatchArgs[1], diskSpaceUpdatedAction);
    /** @type {FileListDirChangedAction} */
    const dirUpdatedAction = {
      action: "FileListDirChangedAction",
      dir: FileListItem.currDir.name,
      currDir: FileListDir(rootPath, false, [
        ZipEntry("", "dir 1", true, 0, 1),
        ZipEntry("", "file 1", false, 2, 3),
      ]),
    };
    deepEqual(dispatchArgs[2], dirUpdatedAction);
  });
});

/**
 * @param {Partial<FileListPanelProps>} props
 * @returns {FileListPanelProps}
 */
function getFileListPanelProps(props = {}) {
  return {
    dispatch: mockFunction(),
    actions: new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    }),
    state: FileListState(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} panelUi
 * @param {FileListPanelProps} props
 */
function assertZipPanel(result, panelUi, props) {
  deepEqual(panelUi.displayName, "ZipPanel");

  assertComponents(
    result.children,

    h(fileListPanelComp, props),
  );
}
