/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").FileListData} FileListData
 * @import { MessageBoxAction } from "@farjs/ui/popup/MessageBoxAction.mjs"
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { FileListDir } from "@farjs/filelist/api/FileListDir.mjs"
 * @import { FileExistsAction } from "../../copymove/FileExistsPopup.mjs"
 * @import { CopyProcessProps } from "../../copymove/CopyProcess.mjs"
 */
import path from "path";
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import FileExistsPopup from "../../copymove/FileExistsPopup.mjs";
import CopyProgressPopup from "../../copymove/CopyProgressPopup.mjs";
import CopyProcess from "../../copymove/CopyProcess.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

CopyProcess.copyProgressPopup = mockComponent(CopyProgressPopup);
CopyProcess.fileExistsPopup = mockComponent(FileExistsPopup);
CopyProcess.messageBoxComp = mockComponent(MessageBox);
CopyProcess.timers = {
  setInterval: mockFunction(),
  clearInterval: mockFunction(),
};

const { copyProgressPopup, fileExistsPopup, messageBoxComp } = CopyProcess;

const resolvedWriteRes = Promise.resolve(undefined);

describe("CopyProcess.test.mjs", () => {
  it("should increment time100ms every 100 ms.", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const to = FileListData(toDispatch, toActions, FileListState());
    const timerId = /** @type {NodeJS.Timer} */ ({});
    let setIntervalArgs = /** @type {any[]} */ ([]);
    const setIntervalMock = mockFunction((...args) => {
      setIntervalArgs = args;
      return timerId;
    });
    let clearIntervalArgs = /** @type {any[]} */ ([]);
    const clearIntervalMock = mockFunction(
      (...args) => (clearIntervalArgs = args)
    );
    const savedTimers = CopyProcess.timers;
    CopyProcess.timers = {
      setInterval: setIntervalMock,
      clearInterval: clearIntervalMock,
    };
    const props = getCopyProcessProps(from, to, {
      items: [],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(setIntervalMock.times, 1);
    assert.deepEqual(setIntervalArgs, [setIntervalArgs[0], 100]);
    const onTimer = setIntervalArgs[0];

    //when & then
    for (let i = 0; i < 10; i++) {
      TestRenderer.act(() => {
        onTimer();
      });
    }
    const progressProps = renderer.root.findByType(copyProgressPopup).props;
    assert.deepEqual(progressProps.timeSeconds, 1);

    //when & then
    for (let i = 0; i < 10; i++) {
      TestRenderer.act(() => {
        onTimer();
      });
    }
    const progressProps2 = renderer.root.findByType(copyProgressPopup).props;
    assert.deepEqual(progressProps2.timeSeconds, 2);

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //then
    assert.deepEqual(setIntervalMock.times, 1);
    assert.deepEqual(clearIntervalMock.times, 1);
    assert.deepEqual(clearIntervalArgs, [timerId]);
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);

    //cleanup
    CopyProcess.timers = savedTimers;
  });

  it("should not increment time100ms when cancel", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions();
    const to = FileListData(toDispatch, toActions, FileListState());
    const timerId = /** @type {NodeJS.Timer} */ ({});
    let setIntervalArgs = /** @type {any[]} */ ([]);
    const setIntervalMock = mockFunction((...args) => {
      setIntervalArgs = args;
      return timerId;
    });
    let clearIntervalArgs = /** @type {any[]} */ ([]);
    const clearIntervalMock = mockFunction(
      (...args) => (clearIntervalArgs = args)
    );
    const savedTimers = CopyProcess.timers;
    CopyProcess.timers = {
      setInterval: setIntervalMock,
      clearInterval: clearIntervalMock,
    };
    const props = getCopyProcessProps(from, to, {
      items: [],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(setIntervalMock.times, 1);
    assert.deepEqual(setIntervalArgs, [setIntervalArgs[0], 100]);
    const onTimer = setIntervalArgs[0];
    const progressProps = renderer.root.findByType(copyProgressPopup).props;
    assert.deepEqual(progressProps.timeSeconds, 0);

    //when
    TestRenderer.act(() => {
      progressProps.onCancel();
    });

    //then
    for (let i = 0; i < 10; i++) {
      TestRenderer.act(() => {
        onTimer();
      });
    }
    const progressProps2 = renderer.root.findByType(copyProgressPopup).props;
    assert.deepEqual(progressProps2.timeSeconds, 0);

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //then
    assert.deepEqual(setIntervalMock.times, 1);
    assert.deepEqual(clearIntervalMock.times, 1);
    assert.deepEqual(clearIntervalArgs, [timerId]);
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);

    //cleanup
    CopyProcess.timers = savedTimers;
  });

  it("should pause copy process and handle Yes action when cancelling", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn = copyFileArgs[3];
    const progressProps = renderer.root.findByType(copyProgressPopup).props;

    //when
    await actAsync(() => {
      progressProps.onCancel();
    });

    //then
    const messageBox = renderer.root.findByType(messageBoxComp);
    /** @type {MessageBoxAction[]} */
    const cancelActions = messageBox.props.actions;
    assert.deepEqual(
      cancelActions.map((_) => ({
        label: _.label,
        triggeredOnClose: _.triggeredOnClose,
      })),
      [
        {
          label: "YES",
          triggeredOnClose: false,
        },
        {
          label: "NO",
          triggeredOnClose: true,
        },
      ]
    );
    assertComponents(
      [messageBox],
      h(messageBoxComp, {
        title: "Operation has been interrupted",
        message: "Do you really want to cancel it?",
        actions: cancelActions,
        style: DefaultTheme.popup.error,
      })
    );
    const yesAction = cancelActions[0];

    //when & then
    await actAsync(() => {
      yesAction.onAction();
    });
    assert.deepEqual(renderer.root.findAllByType(messageBoxComp), []);
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, false);

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should pause copy process and handle No action when cancelling", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn = copyFileArgs[3];
    const progressProps = renderer.root.findByType(copyProgressPopup).props;

    //when
    await actAsync(() => {
      progressProps.onCancel();
    });

    //then
    /** @type {MessageBoxAction[]} */
    const cancelActions =
      renderer.root.findByType(messageBoxComp).props.actions;
    const noAction = cancelActions[cancelActions.length - 1];

    //when & then
    await actAsync(() => {
      noAction.onAction();
    });
    assert.deepEqual(renderer.root.findAllByType(messageBoxComp), []);
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, true);

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item]);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  /** @type {(action: FileExistsAction) => () => Promise<void>} */
  const testFileExistsAction = (action) => async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve1 = () => {};
    const progressP1 = new Promise((res) => (resolve1 = res));
    /** @type {(v: boolean) => void} */
    let resolve2 = () => {};
    const progressP2 = new Promise((res) => (resolve2 = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return copyFile.times === 1 ? progressP1 : progressP2;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item1 = FileListItem("file 1");
    const item2 = FileListItem("file 2");
    const props = getCopyProcessProps(from, to, {
      items: [
        { item: item1, toName: "newName1" },
        { item: item2, toName: "newName2" },
      ],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName1", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item1,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onExistsFn1 = writeFileArgs[2];
    const onProgressFn1 = copyFileArgs[3];

    //when & then
    let onExistsP1 = /** @type {Promise<boolean | undefined> | undefined} */ (
      undefined
    );
    TestRenderer.act(() => {
      onExistsP1 = onExistsFn1(FileListItem("existing.file1"));
    });
    const existsProps1 = renderer.root.findByType(fileExistsPopup).props;
    TestRenderer.act(() => {
      existsProps1.onAction(action);
    });
    assert.deepEqual(renderer.root.findAllByType(fileExistsPopup), []);
    const onExistsRes1 = await onExistsP1;
    const expectedOnExistsRes = (() => {
      switch (action) {
        case "Overwrite":
        case "All":
          return true;
        case "Append":
          return false;
        default:
          return undefined;
      }
    })();
    assert.deepEqual(onExistsRes1, expectedOnExistsRes);

    const result1 = await actAsync(() => {
      return onProgressFn1(123);
    });
    assert.deepEqual(result1, true);
    await actAsync(() => {
      resolve1(result1);
    });
    assert.deepEqual(onTopItem.times, action.startsWith("Skip") ? 0 : 1);
    assert.deepEqual(onTopItemArgs, action.startsWith("Skip") ? [] : [item1]);

    //given
    assert.deepEqual(writeFile.times, 2);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName2", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 2);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item2,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onExistsFn2 = writeFileArgs[2];
    const onProgressFn2 = copyFileArgs[3];

    //when & then
    let onExistsP2 = /** @type {Promise<boolean | undefined> | undefined} */ (
      undefined
    );
    TestRenderer.act(() => {
      onExistsP2 = onExistsFn2(FileListItem("existing.file2"));
    });
    if (!action.endsWith("All")) {
      const existsProps2 = renderer.root.findByType(fileExistsPopup).props;
      TestRenderer.act(() => {
        existsProps2.onAction(action);
      });
    }
    assert.deepEqual(renderer.root.findAllByType(fileExistsPopup), []);
    const onExistsRes2 = await onExistsP2;
    assert.deepEqual(onExistsRes2, expectedOnExistsRes);

    const result2 = await actAsync(() => {
      return onProgressFn2(123);
    });
    assert.deepEqual(result2, true);
    await actAsync(() => {
      resolve2(result2);
    });
    assert.deepEqual(onTopItem.times, action.startsWith("Skip") ? 0 : 2);
    assert.deepEqual(onTopItemArgs, action.startsWith("Skip") ? [] : [item2]);

    //then
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  };

  it(
    "should call onDone when Overwrite in FileExistsPopup",
    testFileExistsAction("Overwrite")
  );

  it(
    "should call onDone when Skip in FileExistsPopup",
    testFileExistsAction("Skip")
  );

  it(
    "should call onDone when Append in FileExistsPopup",
    testFileExistsAction("Append")
  );

  it(
    "should not ask for existing file action again when All",
    testFileExistsAction("All")
  );

  it(
    "should not ask for existing file action again when SkipAll",
    testFileExistsAction("SkipAll")
  );

  it("should not ask for existing file action when canceled", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onExistsFn = writeFileArgs[2];
    const onProgressFn = copyFileArgs[3];
    const progressProps = renderer.root.findByType(copyProgressPopup).props;

    //when & then
    await actAsync(() => {
      progressProps.onCancel();
    });
    /** @type {MessageBoxAction[]} */
    const cancelActions =
      renderer.root.findByType(messageBoxComp).props.actions;
    const yesAction = cancelActions[0];
    await actAsync(() => {
      yesAction.onAction();
    });

    //when & then
    let onExistsP = /** @type {Promise<boolean | undefined> | undefined} */ (
      undefined
    );
    TestRenderer.act(() => {
      onExistsP = onExistsFn(FileListItem("existing.file"));
    });
    assert.deepEqual(renderer.root.findAllByType(fileExistsPopup), []);
    const onExistsRes = await onExistsP;
    assert.deepEqual(onExistsRes, undefined);

    // when & then
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, false);

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should call onDone when onCancel in FileExistsPopup", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    /** @type {(v: boolean) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onExistsFn = writeFileArgs[2];
    const onProgressFn = copyFileArgs[3];

    //when & then
    let onExistsP = /** @type {Promise<boolean | undefined> | undefined} */ (
      undefined
    );
    TestRenderer.act(() => {
      onExistsP = onExistsFn(FileListItem("existing.file"));
    });
    const existsProps = renderer.root.findByType(fileExistsPopup).props;

    //when & then
    TestRenderer.act(() => {
      existsProps.onCancel();
    });
    assert.deepEqual(renderer.root.findAllByType(fileExistsPopup), []);

    //when & then
    const onExistsRes = await onExistsP;
    assert.deepEqual(onExistsRes, undefined);

    // when & then
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, false);

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should not call onTopItem if cancelled when unmount", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(dirList);
    });
    /** @type {(v: string) => void} */
    let resolve = () => {};
    const mkDirsP = new Promise((res) => (resolve = res));
    let mkDirsArgs = /** @type {any[]} */ ([]);
    const mkDirs = mockFunction((...args) => {
      mkDirsArgs = args;
      return mkDirsP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ mkDirs }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const dir = FileListItem("dir 1", true);
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item: dir, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    /** @type {FileListDir} */
    const dirList = { path: "/from/path/dir 1", isRoot: false, items: [item] };
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, ["/from/path", "dir 1"]);
    assert.deepEqual(mkDirs.times, 1);
    assert.deepEqual(mkDirsArgs, [["/to/path", "newName"]]);

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //when & then
    await actAsync(() => {
      resolve("/to/path/newName");
    });
    assert.deepEqual(onTopItem.times, 0);
    assert.deepEqual(onDone.times, 1);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should dispatch actions when failure", async () => {
    //given
    const onTopItem = mockFunction();
    const onDone = mockFunction();
    /** @type {(e: Error) => void} */
    let reject = () => {};
    const progressP = new Promise((_, rej) => (reject = rej));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions({
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = FileListItem("file 1");
    const props = getCopyProcessProps(from, to, {
      items: [{ item, toName: "newName" }],
      total: 12345,
      onTopItem,
      onDone,
    });
    await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs.slice(0, 2), ["/to/path", "newName"]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs.slice(0, 2), ["/from/path", item]);
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
    assert.deepEqual(action.task.message, "Copy/Move Items");
    let resultError = null;
    try {
      await action.task.result;
    } catch (e) {
      resultError = e;
    }
    assert.deepEqual(resultError, error);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should make target dir, copy file, update progress and call onDone", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: any) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(dirList);
    });
    let mkDirsArgs = /** @type {any[]} */ ([]);
    const mkDirs = mockFunction((...args) => {
      mkDirsArgs = args;
      return Promise.resolve("/to/path/newName");
    });
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir }),
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ mkDirs, writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = { ...FileListItem("file 1"), size: 246 };
    const dir = FileListItem("dir 1", true);
    const props = getCopyProcessProps(from, to, {
      items: [{ item: dir, toName: "newName" }],
      onTopItem,
      onDone,
    });
    /** @type {FileListDir} */
    const dirList = { path: "/from/path/dir 1", isRoot: false, items: [item] };
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, ["/from/path", "dir 1"]);
    assert.deepEqual(mkDirs.times, 1);
    assert.deepEqual(mkDirsArgs, [["/to/path", "newName"]]);
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs.slice(0, 2), [
      "/to/path/newName",
      item.name,
    ]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs.slice(0, 2), ["/from/path/dir 1", item]);
    const onProgressFn = copyFileArgs[3];

    //when & then
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "/newName",
      item: "file 1",
      toName: "file 1",
      itemPercent: 50,
      totalPercent: 25,
    });

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [dir]);
    assert.deepEqual(onDone.times, 1);
    assertCopyProcess({
      renderer,
      props,
      toPath: "/newName",
      item: "file 1",
      toName: "file 1",
      itemPercent: 50,
      totalPercent: 25,
    });
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should make target dir, move file, update progress and call onDone", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: any) => void} */
    let resolve = () => {};
    const progressP = new Promise((res) => (resolve = res));
    let readDirArgs = /** @type {any[]} */ ([]);
    const readDir = mockFunction((...args) => {
      readDirArgs = args;
      return Promise.resolve(dirList);
    });
    let mkDirsArgs = /** @type {any[]} */ ([]);
    const mkDirs = mockFunction((...args) => {
      mkDirsArgs = args;
      return Promise.resolve("/to/path/newName");
    });
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return progressP;
    });
    const dispatch = mockFunction();
    let deleteArgs = /** @type {any[]} */ ([]);
    const deleteMock = mockFunction((...args) => {
      deleteArgs.push(...args);
      return Promise.resolve();
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readDir, delete: deleteMock }),
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ mkDirs, writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item = { ...FileListItem("file 1"), size: 246 };
    const dir = FileListItem("dir 1", true);
    const props = getCopyProcessProps(from, to, {
      move: true,
      items: [{ item: dir, toName: "newName" }],
      onTopItem,
      onDone,
    });
    /** @type {FileListDir} */
    const dirList = { path: "/from/path/dir 1", isRoot: false, items: [item] };
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(readDir.times, 1);
    assert.deepEqual(readDirArgs, ["/from/path", "dir 1"]);
    assert.deepEqual(mkDirs.times, 1);
    assert.deepEqual(mkDirsArgs, [["/to/path", "newName"]]);
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs.slice(0, 2), [
      "/to/path/newName",
      item.name,
    ]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs.slice(0, 2), ["/from/path/dir 1", item]);
    const onProgressFn = copyFileArgs[3];
    assertCopyProcess({
      renderer,
      props,
      toPath: "/newName",
      item: "file 1",
      toName: "file 1",
      itemPercent: 0,
      totalPercent: 0,
    });

    //when & then
    const result = await actAsync(() => {
      return onProgressFn(123);
    });
    assert.deepEqual(result, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "/newName",
      item: "file 1",
      toName: "file 1",
      itemPercent: 50,
      totalPercent: 25,
    });

    //when & then
    await actAsync(() => {
      resolve(result);
    });
    assert.deepEqual(deleteMock.times, 2);
    assert.deepEqual(deleteArgs, [
      "/from/path/dir 1",
      [item],
      "/from/path",
      [dir],
    ]);
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [dir]);
    assert.deepEqual(onDone.times, 1);
    assertCopyProcess({
      renderer,
      props,
      toPath: "/newName",
      item: "file 1",
      toName: "file 1",
      itemPercent: 50,
      totalPercent: 25,
    });
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should copy two files and update progress", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: any) => void} */
    let resolve1 = () => {};
    const progressP1 = new Promise((res) => (resolve1 = res));
    /** @type {(v: any) => void} */
    let resolve2 = () => {};
    const progressP2 = new Promise((res) => (resolve2 = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return copyFile.times === 1 ? progressP1 : progressP2;
    });
    const dispatch = mockFunction();
    const actions = new MockFileListActions({ copyFile });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item1 = { ...FileListItem("file 1"), size: 246 };
    const item2 = { ...FileListItem("file 2"), size: 123 };
    const props = getCopyProcessProps(from, to, {
      items: [
        { item: item1, toName: "newName1" },
        { item: item2, toName: "newName2" },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName1", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item1,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn1 = copyFileArgs[3];
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 1",
      toName: "newName1",
      itemPercent: 0,
      totalPercent: 0,
    });

    //when & then
    const result = await actAsync(() => {
      return onProgressFn1(123);
    });
    assert.deepEqual(result, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 1",
      toName: "newName1",
      itemPercent: 50,
      totalPercent: 25,
    });

    //when & then
    await actAsync(() => {
      resolve1(result);
    });
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item1]);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 0,
      totalPercent: 25,
    });
    assert.deepEqual(writeFile.times, 2);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName2", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 2);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item2,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn2 = copyFileArgs[3];

    //when & then
    const result2 = await actAsync(() => {
      return onProgressFn2(123);
    });
    assert.deepEqual(result2, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 100,
      totalPercent: 50,
    });

    //when & then
    await actAsync(() => {
      resolve2(result2);
    });
    assert.deepEqual(onTopItem.times, 2);
    assert.deepEqual(onTopItemArgs, [item2]);
    assert.deepEqual(onDone.times, 1);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 100,
      totalPercent: 50,
    });
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });

  it("should move two files and update progress", async () => {
    //given
    let onTopItemArgs = /** @type {any[]} */ ([]);
    const onTopItem = mockFunction((...args) => (onTopItemArgs = args));
    const onDone = mockFunction();
    /** @type {(v: any) => void} */
    let resolve1 = () => {};
    const progressP1 = new Promise((res) => (resolve1 = res));
    /** @type {(v: any) => void} */
    let resolve2 = () => {};
    const progressP2 = new Promise((res) => (resolve2 = res));
    let writeFileArgs = /** @type {any[]} */ ([]);
    const writeFile = mockFunction((...args) => {
      writeFileArgs = args;
      return resolvedWriteRes;
    });
    let copyFileArgs = /** @type {any[]} */ ([]);
    const copyFile = mockFunction((...args) => {
      copyFileArgs = args;
      return copyFile.times === 1 ? progressP1 : progressP2;
    });
    const dispatch = mockFunction();
    let deleteArgs = /** @type {any[]} */ ([]);
    const deleteMock = mockFunction((...args) => {
      deleteArgs = args;
      return Promise.resolve();
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ delete: deleteMock }),
      copyFile,
    });
    const from = FileListData(dispatch, actions, FileListState());
    const toDispatch = mockFunction();
    const toActions = new MockFileListActions({
      api: new MockFileListApi({ writeFile }),
    });
    const to = FileListData(toDispatch, toActions, FileListState());
    const item1 = { ...FileListItem("file 1"), size: 246 };
    const item2 = { ...FileListItem("file 2"), size: 123 };
    const props = getCopyProcessProps(from, to, {
      move: true,
      items: [
        { item: item1, toName: "newName1" },
        { item: item2, toName: "newName2" },
      ],
      onTopItem,
      onDone,
    });
    const renderer = await actAsync(() => {
      return TestRenderer.create(withThemeContext(h(CopyProcess, props)));
    });
    assert.deepEqual(writeFile.times, 1);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName1", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 1);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item1,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn1 = copyFileArgs[3];
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 1",
      toName: "newName1",
      itemPercent: 0,
      totalPercent: 0,
    });

    //when & then
    const result = await actAsync(() => {
      return onProgressFn1(123);
    });
    assert.deepEqual(result, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 1",
      toName: "newName1",
      itemPercent: 50,
      totalPercent: 25,
    });

    //when & then
    await actAsync(() => {
      resolve1(result);
    });
    assert.deepEqual(deleteMock.times, 1);
    assert.deepEqual(deleteArgs, ["/from/path", [item1]]);
    assert.deepEqual(onTopItem.times, 1);
    assert.deepEqual(onTopItemArgs, [item1]);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 0,
      totalPercent: 25,
    });
    assert.deepEqual(writeFile.times, 2);
    assert.deepEqual(writeFileArgs, ["/to/path", "newName2", writeFileArgs[2]]);
    assert.deepEqual(copyFile.times, 2);
    assert.deepEqual(copyFileArgs, [
      "/from/path",
      item2,
      copyFileArgs[2],
      copyFileArgs[3],
    ]);
    const onProgressFn2 = copyFileArgs[3];

    //when & then
    const result2 = await actAsync(() => {
      return onProgressFn2(123);
    });
    assert.deepEqual(result2, true);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 100,
      totalPercent: 50,
    });

    //when & then
    await actAsync(() => {
      resolve2(result2);
    });
    assert.deepEqual(deleteMock.times, 2);
    assert.deepEqual(deleteArgs, ["/from/path", [item2]]);
    assert.deepEqual(onTopItem.times, 2);
    assert.deepEqual(onTopItemArgs, [item2]);
    assert.deepEqual(onDone.times, 1);
    assertCopyProcess({
      renderer,
      props,
      toPath: "",
      item: "file 2",
      toName: "newName2",
      itemPercent: 100,
      totalPercent: 50,
    });
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(toDispatch.times, 0);
  });
});

/**
 * @param {FileListData} from
 * @param {FileListData} to
 * @param {Partial<CopyProcessProps>} props
 * @returns {CopyProcessProps}
 */
function getCopyProcessProps(from, to, props = {}) {
  return {
    from,
    to,
    move: false,
    fromPath: "/from/path",
    items: [{ item: FileListItem("dir 1", true), toName: "dir 1" }],
    toPath: "/to/path",
    total: 492,
    onTopItem: mockFunction(),
    onDone: mockFunction(),
    ...props,
  };
}

/**
 * @param {{
 *  readonly renderer: TestRenderer.ReactTestRenderer;
 *  readonly props: CopyProcessProps;
 *  readonly toPath: string;
 *  readonly item: string;
 *  readonly toName: string;
 *  readonly itemPercent: number;
 *  readonly totalPercent: number;
 * }} params
 */
function assertCopyProcess({
  renderer,
  props,
  toPath,
  item,
  toName,
  itemPercent,
  totalPercent,
}) {
  assert.deepEqual(CopyProcess.displayName, "CopyProcess");

  renderer.update(withThemeContext(h(CopyProcess, props)));

  assertComponents(
    renderer.root.children,
    h(copyProgressPopup, {
      move: props.move,
      item,
      to: path.join(props.toPath, toPath, toName),
      itemPercent,
      total: props.total,
      totalPercent,
      timeSeconds: 0,
      leftSeconds: 0,
      bytesPerSecond: 0,
      onCancel: mockFunction(),
    })
  );
}
