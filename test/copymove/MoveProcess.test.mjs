/**
 * @import { MessageBoxAction } from "@farjs/ui/popup/MessageBoxAction.mjs"
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { MoveProcessProps } from "../../copymove/MoveProcess.mjs"
 */
import path from "path";
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MoveProcess from "../../copymove/MoveProcess.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

MoveProcess.statusPopupComp = mockComponent(StatusPopup);
MoveProcess.messageBoxComp = mockComponent(MessageBox);

const { statusPopupComp, messageBoxComp } = MoveProcess;

describe("MoveProcess.test.mjs", () => {
  it("should call onTopItem/onDone when success", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => {
      onTopItemArgs.push(...args);
    });
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs = args;
      return false;
    });
    /** @type {(v: any) => void} */
    let resolve = () => {};
    const renameP = new Promise((res) => (resolve = res));
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs.push(...args);
      return renameP;
    });
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = FileListItem("dir 1", true);
    const item2 = { ...FileListItem("file 1"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: "newName1" },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "dir 1"),
      path.join(props.toPath, "newName1"),
    ]);
    assertMoveProcess(renderer.root, "dir 1");

    // when
    await actAsync(() => {
      resolve(undefined);
    });

    //then
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [path.join(props.toPath, "file 1")]);
    assert.deepEqual(rename.times, 2);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "dir 1"),
      path.join(props.toPath, "newName1"),
      path.join(props.fromPath, "file 1"),
      path.join(props.toPath, "file 1"),
    ]);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(onTopItem.times, 2);
    assert.deepEqual(onTopItemArgs, [item1, item2]);
    assertMoveProcess(renderer.root, "file 1");
  });

  it("should dispatch actions when failure", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    /** @type {(e: Error) => void} */
    let reject = () => {};
    const renameP = new Promise((_, rej) => (reject = rej));
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs = args;
      return renameP;
    });
    MoveProcess.fs = { existsSync: mockFunction(), rename };
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions();
    const item1 = FileListItem("dir 1", true);
    const item2 = { ...FileListItem("file 1"), size: 10 };
    const props = getMoveProcessProps({
      dispatch,
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "dir 1"),
      path.join(props.toPath, "dir 1"),
    ]);
    const error = Error("test error");

    //when
    await actAsync(() => {
      reject(error);
    });

    //then
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Moving items");
    let resultError = null;
    try {
      await action.task.result;
    } catch (e) {
      resultError = e;
    }
    assert.deepEqual(resultError, error);
  });

  it("should handle onClose action", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => {
      onTopItemArgs.push(...args);
    });
    const onDone = mockFunction();
    const existsSync = mockFunction();
    /** @type {(v: any) => void} */
    let resolve = () => {};
    const renameP = new Promise((res) => (resolve = res));
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs.push(...args);
      return renameP;
    });
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = FileListItem("dir 1", true);
    const item2 = { ...FileListItem("file 1"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "dir 1"),
      path.join(props.toPath, "dir 1"),
    ]);
    assertMoveProcess(renderer.root, "dir 1");
    const popupProps = renderer.root.findByType(statusPopupComp).props;

    // when
    popupProps.onClose();

    await actAsync(() => {
      resolve(undefined);
    });

    //then
    assert.deepEqual(existsSync.times, 0);
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "dir 1"),
      path.join(props.toPath, "dir 1"),
    ]);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item1]);
    assertMoveProcess(renderer.root, "dir 1");
  });

  it("should render item exists message and handle Cancel action", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs = args;
      return true;
    });
    const rename = mockFunction();
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = { ...FileListItem("file 1"), size: 1 };
    const item2 = { ...FileListItem("file 2"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });

    // when
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });

    //then
    const newPath = path.join(props.toPath, "file 1");
    assertMoveProcess(renderer.root, "file 1", newPath);
    const messageBox = renderer.root.findByType(messageBoxComp).props;
    const cancelAction = messageBox.actions[messageBox.actions.length - 1];

    // when
    await actAsync(() => {
      cancelAction.onAction();
    });

    //then
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [newPath]);
    assert.deepEqual(rename.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(onTopItem.times, 0);
    assertMoveProcess(renderer.root, "file 1");
  });

  it("should render item exists message and handle Overwrite action", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => {
      onTopItemArgs = args;
    });
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs = args;
      return true;
    });
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs = args;
      return Promise.resolve();
    });
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = { ...FileListItem("file 1"), size: 1 };
    const item2 = { ...FileListItem("file 2"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    const newPath1 = path.join(props.toPath, "file 1");
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [newPath1]);
    assertMoveProcess(renderer.root, "file 1", newPath1);
    const overwrite1 =
      renderer.root.findByType(messageBoxComp).props.actions[0];

    // when
    await actAsync(() => {
      overwrite1.onAction();
    });

    //then
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "file 1"),
      newPath1,
    ]);
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item1]);

    const newPath2 = path.join(props.toPath, "file 2");
    assert.deepEqual(existsSync.times, 2);
    assert.deepEqual(existsSyncArgs, [newPath2]);
    assertMoveProcess(renderer.root, "file 2", newPath2);
    const overwrite2 =
      renderer.root.findByType(messageBoxComp).props.actions[0];

    // when
    await actAsync(() => {
      overwrite2.onAction();
    });

    //then
    assert.deepEqual(rename.times, 2);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "file 2"),
      newPath2,
    ]);
    assert.deepEqual(onTopItem.times, 2);
    assert.deepEqual(onTopItemArgs, [item2]);
    assert.deepEqual(onDone.times, 1);
    assertMoveProcess(renderer.root, "file 2");
  });

  it("should render item exists message and handle All action", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => {
      onTopItemArgs.push(...args);
    });
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs = args;
      return true;
    });
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs.push(...args);
      return Promise.resolve();
    });
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = { ...FileListItem("file 1"), size: 1 };
    const item2 = { ...FileListItem("file 2"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    const newPath1 = path.join(props.toPath, "file 1");
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [newPath1]);
    assertMoveProcess(renderer.root, "file 1", newPath1);
    const allAction = renderer.root.findByType(messageBoxComp).props.actions[1];

    // when
    await actAsync(() => {
      allAction.onAction();
    });

    //then
    const newPath2 = path.join(props.toPath, "file 2");
    assert.deepEqual(existsSync.times, 2);
    assert.deepEqual(existsSyncArgs, [newPath2]);
    assert.deepEqual(rename.times, 2);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "file 1"),
      newPath1,
      path.join(props.fromPath, "file 2"),
      newPath2,
    ]);
    assert.deepEqual(onTopItem.times, 2);
    assert.deepEqual(onTopItemArgs, [item1, item2]);
    assert.deepEqual(onDone.times, 1);
    assertMoveProcess(renderer.root, "file 2");
  });

  it("should render item exists message and handle Skip action", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs = args;
      return true;
    });
    const rename = mockFunction();
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = { ...FileListItem("file 1"), size: 1 };
    const item2 = { ...FileListItem("file 2"), size: 10 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    const newPath1 = path.join(props.toPath, "file 1");
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [newPath1]);
    assertMoveProcess(renderer.root, "file 1", newPath1);
    const skip1 = renderer.root.findByType(messageBoxComp).props.actions[2];

    // when
    await actAsync(() => {
      skip1.onAction();
    });

    //then
    const newPath2 = path.join(props.toPath, "file 2");
    assert.deepEqual(existsSync.times, 2);
    assert.deepEqual(existsSyncArgs, [newPath2]);
    assertMoveProcess(renderer.root, "file 2", newPath2);
    const skip2 = renderer.root.findByType(messageBoxComp).props.actions[2];

    // when
    await actAsync(() => {
      skip2.onAction();
    });

    //then
    assert.deepEqual(existsSync.times, 2);
    assert.deepEqual(rename.times, 0);
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assertMoveProcess(renderer.root, "file 2");
  });

  it("should render item exists message and handle Skip all action", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => {
      onTopItemArgs.push(...args);
    });
    const onDone = mockFunction();
    let existsSyncArgs = /** @type {any[]} */ ([]);
    const existsSync = mockFunction((...args) => {
      existsSyncArgs.push(...args);
      return existsSync.times < 3 ? true : false;
    });
    let renameArgs = /** @type {any[]} */ ([]);
    const rename = mockFunction((...args) => {
      renameArgs.push(...args);
      return Promise.resolve();
    });
    MoveProcess.fs = { existsSync, rename };
    const actions = new MockFileListActions();
    const item1 = { ...FileListItem("file 1"), size: 1 };
    const item2 = { ...FileListItem("file 2"), size: 10 };
    const item3 = { ...FileListItem("file 3"), size: 11 };
    const props = getMoveProcessProps({
      actions,
      items: [
        { item: item1, toName: item1.name },
        { item: item2, toName: item2.name },
        { item: item3, toName: item3.name },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(MoveProcess, props)));
    });
    const newPath1 = path.join(props.toPath, "file 1");
    assert.deepEqual(existsSync.times, 1);
    assert.deepEqual(existsSyncArgs, [newPath1]);
    assertMoveProcess(renderer.root, "file 1", newPath1);
    const skipAllAction =
      renderer.root.findByType(messageBoxComp).props.actions[3];

    // when
    await actAsync(() => {
      skipAllAction.onAction();
    });

    //then
    const newPath2 = path.join(props.toPath, "file 2");
    const newPath3 = path.join(props.toPath, "file 3");
    assert.deepEqual(existsSync.times, 3);
    assert.deepEqual(existsSyncArgs, [newPath1, newPath2, newPath3]);
    assert.deepEqual(rename.times, 1);
    assert.deepEqual(renameArgs, [
      path.join(props.fromPath, "file 3"),
      newPath3,
    ]);
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item3]);
    assert.deepEqual(onDone.times, 1);
    assertMoveProcess(renderer.root, "file 3");
  });
});

/**
 * @param {Partial<MoveProcessProps>} props
 * @returns {MoveProcessProps}
 */
function getMoveProcessProps(props = {}) {
  return {
    dispatch: mockFunction(),
    actions: new MockFileListActions(),
    fromPath: "/from/path",
    items: [{ item: FileListItem("dir 1", true), toName: "dir 1" }],
    toPath: "/to/path",
    onTopItem: mockFunction(),
    onDone: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {string} currItem
 * @param {string} [existing]
 */
function assertMoveProcess(result, currItem, existing) {
  assert.deepEqual(MoveProcess.displayName, "MoveProcess");

  const messageBoxActions = existing
    ? (() => {
        const messageBox = result.findByType(messageBoxComp).props;
        /** @type {MessageBoxAction[]} */
        const actions = messageBox.actions;
        assert.deepEqual(
          actions.map((_) => [_.label, _.triggeredOnClose]),
          [
            ["Overwrite", false],
            ["All", false],
            ["Skip", false],
            ["Skip all", false],
            ["Cancel", true],
          ]
        );
        return actions;
      })()
    : undefined;

  assertComponents(
    result.children,
    ...[
      h(statusPopupComp, {
        text: `Moving item\n${currItem}`,
        title: "Move",
        onClose: mockFunction(),
      }),

      existing && messageBoxActions
        ? h(messageBoxComp, {
            title: "Warning",
            message: `File already exists.\nDo you want to overwrite it's content?\n\n${existing}`,
            actions: messageBoxActions,
            style: DefaultTheme.popup.error,
          })
        : null,
    ].filter((_) => _ !== null)
  );
}
