/**
 * @template T
 * @typedef {import("@farjs/ui/task/TaskAction.mjs").TaskAction<T>} TaskAction
 */
/**
 * @import { MessageBoxAction } from "@farjs/ui/popup/MessageBoxAction.mjs"
 * @import { FileListAction } from "@farjs/filelist/FileListActions.mjs"
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 */
import path from "path";
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import CopyItemsPopup from "../../copymove/CopyItemsPopup.mjs";
import CopyItemsStats from "../../copymove/CopyItemsStats.mjs";
import CopyProcess from "../../copymove/CopyProcess.mjs";
import MoveProcess from "../../copymove/MoveProcess.mjs";
import CopyMoveUi from "../../copymove/CopyMoveUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

CopyMoveUi.copyItemsPopup = mockComponent(CopyItemsPopup);
CopyMoveUi.copyItemsStats = mockComponent(CopyItemsStats);
CopyMoveUi.messageBoxComp = mockComponent(MessageBox);
CopyMoveUi.moveProcessComp = mockComponent(MoveProcess);
CopyMoveUi.copyProcessComp = mockComponent(CopyProcess);

const {
  copyItemsPopup,
  copyItemsStats,
  messageBoxComp,
  moveProcessComp,
  copyProcessComp,
} = CopyMoveUi;

describe("CopyMoveUi.test.mjs", () => {
  it("should show CopyItemsStats when copy", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
    });
    const currDir = FileListDir("/folder", false, [
      FileListItem("dir 1", true),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(
      comp,
      pluginUi,
      props,
      false,
      toState.currDir.path,
      currDir.items
    );
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/to/path", false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);

    assertComponents(
      comp.children,
      h(copyItemsStats, {
        dispatch,
        actions,
        fromPath: currDir.path,
        items: currDir.items,
        title: "Copy",
        onDone: mockFunction(),
        onCancel: mockFunction(),
      })
    );
    const statsProps = comp.findByType(copyItemsStats).props;

    //when
    statsProps.onCancel();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should show CopyItemsStats when move", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    let getDriveRootArgs = /** @type {any[]} */ ([]);
    const getDriveRoot = mockFunction((...args) => {
      getDriveRootArgs.push(args);
      return Promise.resolve(undefined);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir, getDriveRoot }),
    });
    const currDir = FileListDir("/folder", false, [
      FileListItem("dir 1", true),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(
      comp,
      pluginUi,
      props,
      true,
      toState.currDir.path,
      currDir.items
    );
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/to/path", false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(getDriveRoot.times, 2);
    assert.deepEqual(getDriveRootArgs, [[currDir.path], [toDir.path]]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);

    assertComponents(
      comp.children,
      h(copyItemsStats, {
        dispatch,
        actions,
        fromPath: currDir.path,
        items: currDir.items,
        title: "Move",
        onDone: mockFunction(),
        onCancel: mockFunction(),
      })
    );
    const statsProps = comp.findByType(copyItemsStats).props;

    //when
    statsProps.onCancel();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render error popup if same path", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [item]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(
      comp,
      pluginUi,
      props,
      false,
      toState.currDir.path,
      currDir.items
    );
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/folder", false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);

    /** @type {MessageBoxAction[]} */
    const msgBoxActions = comp.findByType(messageBoxComp).props.actions;
    assert.deepEqual(
      msgBoxActions.map((_) => _.label),
      ["OK"]
    );
    assertComponents(
      comp.children,
      h(messageBoxComp, {
        title: "Error",
        message: `Cannot copy the item\n${item.name}\nonto itself`,
        actions: msgBoxActions,
        style: DefaultTheme.popup.error,
      })
    );

    //when
    msgBoxActions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render error popup when move into itself", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    let getDriveRootArgs = /** @type {any[]} */ ([]);
    const getDriveRoot = mockFunction((...args) => {
      getDriveRootArgs.push(args);
      return Promise.resolve(undefined);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir, getDriveRoot }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("folder", false, [item]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(
      comp,
      pluginUi,
      props,
      true,
      toState.currDir.path,
      currDir.items
    );
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir(path.join("folder", "dir 1"), false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(getDriveRoot.times, 2);
    assert.deepEqual(getDriveRootArgs, [[currDir.path], [toDir.path]]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);

    /** @type {MessageBoxAction[]} */
    const msgBoxActions = comp.findByType(messageBoxComp).props.actions;
    assert.deepEqual(
      msgBoxActions.map((_) => _.label),
      ["OK"]
    );
    assertComponents(
      comp.children,
      h(messageBoxComp, {
        title: "Error",
        message: `Cannot move the item\n${item.name}\ninto itself`,
        actions: msgBoxActions,
        style: DefaultTheme.popup.error,
      })
    );

    //when
    msgBoxActions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render error popup when copy into itself in sub-folder", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("folder", false, [item]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(
      comp,
      pluginUi,
      props,
      false,
      toState.currDir.path,
      currDir.items
    );
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir(path.join("folder", "dir 1", "dir 2"), false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);

    /** @type {MessageBoxAction[]} */
    const msgBoxActions = comp.findByType(messageBoxComp).props.actions;
    assert.deepEqual(
      msgBoxActions.map((_) => _.label),
      ["OK"]
    );
    assertComponents(
      comp.children,
      h(messageBoxComp, {
        title: "Error",
        message: `Cannot copy the item\n${item.name}\ninto itself`,
        actions: msgBoxActions,
        style: DefaultTheme.popup.error,
      })
    );

    //when
    msgBoxActions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render MoveProcess when move within same drive", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    let getDriveRootArgs = /** @type {any[]} */ ([]);
    const getDriveRoot = mockFunction((...args) => {
      getDriveRootArgs.push(args);
      return Promise.resolve(driveRoot);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir, getDriveRoot }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      item,
      FileListItem("file 1"),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, true, toState.currDir.path, [item]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/to/path", false, []);
    const to = "test to path";
    const driveRoot = "same";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(getDriveRoot.times, 2);
    assert.deepEqual(getDriveRootArgs, [[currDir.path], [toDir.path]]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, true]);

    assertComponents(
      comp.children,
      h(moveProcessComp, {
        dispatch,
        actions,
        fromPath: currDir.path,
        items: [{ item, toName: item.name }],
        toPath: toDir.path,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render MoveProcess when move inplace", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      item,
      FileListItem("file 1"),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveInplace",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, true, item.name, [item]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(dispatch.times, 0);

    assertComponents(
      comp.children,
      h(moveProcessComp, {
        dispatch,
        actions,
        fromPath: currDir.path,
        items: [{ item, toName: to }],
        toPath: currDir.path,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render CopyProcess when copy inplace", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      item,
      FileListItem("file 1"),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyInplace",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, false, item.name, [item]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(dispatch.times, 0);
    const statsPopup = comp.findByType(copyItemsStats).props;
    const total = 123456789;

    //when
    await actAsync(() => {
      statsPopup.onDone(total);
    });

    //then
    assertComponents(
      comp.children,
      h(copyProcessComp, {
        from: { dispatch, actions, state },
        to: { dispatch, actions, state },
        move: false,
        fromPath: currDir.path,
        items: [{ item, toName: to }],
        toPath: currDir.path,
        total,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render CopyProcess when move", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    let getDriveRootArgs = /** @type {any[]} */ ([]);
    const getDriveRoot = mockFunction((...args) => {
      getDriveRootArgs.push(args);
      return Promise.resolve(undefined);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir, getDriveRoot }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      item,
      FileListItem("file 1"),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, true, toState.currDir.path, [item]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/folder/dir to", false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [currDir.path, to]);
    assert.deepEqual(getDriveRoot.times, 2);
    assert.deepEqual(getDriveRootArgs, [[currDir.path], [toDir.path]]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);
    const statsPopup = comp.findByType(copyItemsStats).props;
    const total = 123456789;

    //when
    await actAsync(() => {
      statsPopup.onDone(total);
    });

    //then
    assertComponents(
      comp.children,
      h(copyProcessComp, {
        from: { dispatch, actions, state },
        to: { dispatch: toDispatch, actions: toActions, state: toState },
        move: true,
        fromPath: currDir.path,
        items: [{ item, toName: item.name }],
        toPath: toDir.path,
        total,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render CopyProcess when move from virtual FS", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
    const item = FileListItem("dir 1", true);
    const currDir = FileListDir("/folder", false, [
      item,
      FileListItem("file 1"),
    ]);
    const state = { ...FileListState(), currDir };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
    const comp = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, true, toState.currDir.path, [item]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const to = "test/to/path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(dispatch.times, 0);
    const statsPopup = comp.findByType(copyItemsStats).props;
    const total = 123456789;

    //when
    await actAsync(() => {
      statsPopup.onDone(total);
    });

    //then
    assertComponents(
      comp.children,
      h(copyProcessComp, {
        from: { dispatch, actions, state },
        to: { dispatch: toDispatch, actions: toActions, state: toState },
        move: true,
        fromPath: currDir.path,
        items: [{ item, toName: item.name }],
        toPath: to,
        total,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should dispatch FileListParamsChangedAction if selected when onDone", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => {
      dispatchArgs.push(...args);
    });
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(toDir);
    });
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs.push(args);
      return updateDir.times === 1 ? leftAction : rightAction;
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
      updateDir,
    });
    const dir = FileListItem("dir 1", true);
    const leftDir = FileListDir("/left/dir", false, [
      FileListItem.up,
      dir,
      FileListItem("file 1"),
    ]);
    const state = {
      ...FileListState(),
      index: 1,
      currDir: leftDir,
      selectedNames: new Set([dir.name, "file 1"]),
    };
    let toDispatchArgs = /** @type {any[]} */ ([]);
    const toDispatch = mockFunction((...args) => {
      toDispatchArgs.push(...args);
    });
    const toActions = new MockFileListActions({ updateDir });
    const rightDir = FileListDir("/right/dir", false, [
      FileListItem("dir 2", true),
    ]);
    const toState = { ...FileListState(), currDir: rightDir };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
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
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();
    const comp = TestRenderer.create(
      withHistoryProvider(withThemeContext(h(pluginUi, props)), provider)
    ).root;
    const items = leftDir.items.filter((_) => state.selectedNames.has(_.name));
    assertCopyMoveUi(comp, pluginUi, props, false, toState.currDir.path, items);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const toDir = FileListDir("/to/path/dir 1", false, []);
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, [leftDir.path, to]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Resolving target dir");
    assert.deepEqual(await action.task.result, [toDir.path, false]);
    const statsPopup = comp.findByType(copyItemsStats).props;
    const total = 123;

    //when
    await actAsync(() => {
      statsPopup.onDone(total);
    });

    //then
    assertComponents(
      comp.children,
      h(copyProcessComp, {
        from: { dispatch, actions, state },
        to: { dispatch: toDispatch, actions: toActions, state: toState },
        move: false,
        fromPath: leftDir.path,
        items: items.map((item) => ({ item, toName: item.name })),
        toPath: toDir.path,
        total,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const leftAction = TaskAction(
      Task("Updating", Promise.resolve(updatedDir))
    );
    const rightAction = TaskAction(
      Task("Updating", Promise.resolve(updatedDir))
    );
    const progressPopup = comp.findByType(copyProcessComp).props;

    //when
    await actAsync(() => {
      progressPopup.onTopItem(dir);
      progressPopup.onDone();
    });

    //then
    await getP;
    await saveP;
    await leftAction.task.result;
    await rightAction.task.result;

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: to }]);
    assert.deepEqual(updateDir.times, 2);
    assert.deepEqual(updateDirArgs, [
      [dispatch, leftDir.path],
      [toDispatch, rightDir.path],
    ]);
    assert.deepEqual(dispatch.times, 3);
    /** @type {FileListAction} */
    const expectedAction = {
      action: "FileListParamsChangedAction",
      offset: 0,
      index: 1,
      selectedNames: new Set(["file 1"]),
    };
    assert.deepEqual(dispatchArgs[1], expectedAction);
    assert.deepEqual(dispatchArgs[2], leftAction);

    assert.deepEqual(toDispatch.times, 1);
    assert.deepEqual(toDispatchArgs, [rightAction]);
  });

  it("should dispatch FileListItemCreatedAction if inplace when onDone", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => {
      dispatchArgs.push(...args);
    });
    let updateDirArgs = /** @type {any[]} */ ([]);
    const updateDir = mockFunction((...args) => {
      updateDirArgs = args;
      return leftAction;
    });
    const actions = new MockFileListActions({ updateDir });
    const dir = FileListItem("dir 1", true);
    const leftDir = FileListDir("/left/dir", false, [
      FileListItem.up,
      dir,
      FileListItem("file 1"),
    ]);
    const state = {
      ...FileListState(),
      index: 1,
      currDir: leftDir,
      selectedNames: new Set(["file 1"]),
    };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const rightDir = FileListDir("/right/dir", false, [
      FileListItem("dir 2", true),
    ]);
    const toState = { ...FileListState(), currDir: rightDir };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyInplace",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });
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
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();
    const comp = TestRenderer.create(
      withHistoryProvider(withThemeContext(h(pluginUi, props)), provider)
    ).root;
    assertCopyMoveUi(comp, pluginUi, props, false, dir.name, [dir]);
    const copyPopupProps = comp.findByType(copyItemsPopup).props;
    const to = "test to path";

    //when
    await actAsync(() => {
      copyPopupProps.onAction(to);
    });

    //then
    assert.deepEqual(dispatch.times, 0);
    const statsPopup = comp.findByType(copyItemsStats).props;
    const total = 123;

    //when
    await actAsync(() => {
      statsPopup.onDone(total);
    });

    //then
    assertComponents(
      comp.children,
      h(copyProcessComp, {
        from: { dispatch, actions, state },
        to: { dispatch, actions, state },
        move: false,
        fromPath: leftDir.path,
        items: [{ item: dir, toName: to }],
        toPath: leftDir.path,
        total,
        onTopItem: mockFunction(),
        onDone: mockFunction(),
      })
    );
    const updatedDir = FileListDir("/updated/dir", false, [
      FileListItem("file 1"),
    ]);
    const leftAction = TaskAction(
      Task("Updating", Promise.resolve(updatedDir))
    );
    const progressPopup = comp.findByType(copyProcessComp).props;

    //when
    await actAsync(() => {
      progressPopup.onTopItem(dir);
      progressPopup.onDone();
    });

    //then
    await getP;
    await saveP;
    await leftAction.task.result;

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: to }]);
    assert.deepEqual(updateDir.times, 1);
    assert.deepEqual(updateDirArgs, [dispatch, leftDir.path]);
    assert.deepEqual(dispatch.times, 2);
    /** @type {FileListAction} */
    const expectedAction = {
      action: "FileListItemCreatedAction",
      name: to,
      currDir: updatedDir,
    };
    assert.deepEqual(dispatchArgs[0], leftAction);
    assert.deepEqual(dispatchArgs[1], expectedAction);

    assert.deepEqual(toDispatch.times, 0);
  });

  it("should render CopyItemsPopup(items=single) when copy to different dir", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("dir 1", true);
    const state = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, [item]),
    };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = {
      ...FileListState(),
      currDir: FileListDir("/test-path", false, [item]),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });

    //when
    const result = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;

    //then
    assertCopyMoveUi(result, pluginUi, props, false, "/test-path", [item]);
  });

  it("should render CopyItemsPopup(items=single) when copy to the same dir", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("dir 1", true);
    const state = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, [item]),
    };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, [item]),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowCopyToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });

    //when
    const result = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;

    //then
    assertCopyMoveUi(result, pluginUi, props, false, item.name, [item]);
  });

  it("should render CopyItemsPopup(items=multi) when move to different dir", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const items = [FileListItem("file 1"), FileListItem("dir 1", true)];
    const state = {
      ...FileListState(),
      currDir: FileListDir("/test-path", false, items),
      selectedNames: new Set(["file 1", "dir 1"]),
    };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, []),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });

    //when
    const result = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;

    //then
    assertCopyMoveUi(result, pluginUi, props, true, "/folder", items);
  });

  it("should render CopyItemsPopup(items=multi) when move to the same dir", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const items = [FileListItem("file 1"), FileListItem("dir 1", true)];
    const state = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, items),
      selectedNames: new Set(["file 1", "dir 1"]),
    };
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const toState = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, []),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveToTarget",
      from: FileListData(dispatch, actions, state),
      maybeTo: FileListData(toDispatch, toActions, toState),
    });

    //when
    const result = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;

    //then
    assertCopyMoveUi(result, pluginUi, props, true, "/folder", items);
  });

  it("should render CopyItemsPopup(items=0) when move inplace", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: FileListDir("/folder", false, []),
    };
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const pluginUi = CopyMoveUi({
      show: "ShowMoveInplace",
      from: FileListData(dispatch, actions, state),
    });

    //when
    const result = TestRenderer.create(
      withHistoryProvider(
        withThemeContext(h(pluginUi, props)),
        new MockHistoryProvider()
      )
    ).root;

    //then
    assertCopyMoveUi(result, pluginUi, props, true, "", []);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} pluginUi
 * @param {FileListPluginUiProps} props
 * @param {boolean} isMove
 * @param {string} path
 * @param {readonly FileListItem[]} items
 */
function assertCopyMoveUi(result, pluginUi, props, isMove, path, items) {
  assert.deepEqual(pluginUi.displayName, "CopyMoveUi");

  assert.deepEqual(result.children.length, 1);
  assert.deepEqual(
    result.findByType(copyItemsPopup).props.onCancel === props.onClose,
    true
  );

  assertComponents(
    result.children,
    h(copyItemsPopup, {
      move: isMove,
      path,
      items,
      onAction: mockFunction(),
      onCancel: mockFunction(),
    })
  );
}
