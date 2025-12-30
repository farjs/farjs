/**
 * @template T
 * @typedef {import("@farjs/ui/task/TaskAction.mjs").TaskAction<T>} TaskAction
 */
/**
 * @typedef {import("@farjs/blessed").Widgets.Events.IKeyEventArg & {
 *    data?: any
 * }} IKeyEventArg
 * @typedef {import("../../../app/filelist/FileListPluginHandler.mjs").FileListPluginHandler} FileListPluginHandler
 */
import path from "path";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockFileSource from "@farjs/filelist/api/MockFileSource.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileListPluginHandler from "../../../app/filelist/FileListPluginHandler.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fsComp = () => null;
const stacks = WithStacksProps(
  WithStacksData(new PanelStack(true, [], mockFunction())),
  WithStacksData(new PanelStack(false, [], mockFunction()))
);

describe("FileListPluginHandler.test.mjs", () => {
  it("should do nothing if no stack item data when openCurrItem", () => {
    //given
    const dispatch = mockFunction();
    const onFileTrigger = mockFunction();
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const stack = new PanelStack(
      true,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );

    //when
    handler.openCurrItem(dispatch, stack);

    //then
    assert.deepEqual(onFileTrigger.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should do nothing if non-local FS when openCurrItem", () => {
    //given
    const dispatch = mockFunction();
    const onFileTrigger = mockFunction();
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
    const state = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, [FileListItem("file 1")]),
    };
    const stack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );

    //when
    handler.openCurrItem(dispatch, stack);

    //then
    assert.deepEqual(onFileTrigger.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should do nothing if dir when openCurrItem", () => {
    //given
    const dispatch = mockFunction();
    const onFileTrigger = mockFunction();
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, [FileListItem("dir 1", true)]),
    };
    const stack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );

    //when
    handler.openCurrItem(dispatch, stack);

    //then
    assert.deepEqual(onFileTrigger.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should dispatch error task if failed open file when openCurrItem", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const onFileTrigger = mockFunction();
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const error = Error("test error");
    let readFileArgs = /** @type {any[]} */ ([]);
    const readFile = mockFunction((...args) => {
      readFileArgs = args;
      return Promise.reject(error);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readFile }),
    });
    const item = FileListItem("file 1");
    const state = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, [item]),
    };
    const stack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );

    //when
    handler.openCurrItem(dispatch, stack);

    //then
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(onFileTrigger.times, 0);
    assert.deepEqual(readFile.times, 1);
    assert.deepEqual(readFileArgs, [state.currDir.path, item, 0]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Opening File Plugin");
    let capturedError = null;
    try {
      await action.task.result;
    } catch (err) {
      capturedError = err;
    }
    assert.deepEqual(capturedError, error);
  });

  it("should open plugin file item when openCurrItem", async () => {
    //given
    const dispatch = mockFunction();
    const pluginComp = () => null;
    const pluginStackItem = new PanelStackItem(pluginComp);
    let onFileTrigger1Args = /** @type {any[]} */ ([]);
    const onFileTrigger1 = mockFunction((...args) => {
      onFileTrigger1Args = args;
      return Promise.resolve(undefined);
    });
    let onFileTrigger2Args = /** @type {any[]} */ ([]);
    const onFileTrigger2 = mockFunction((...args) => {
      onFileTrigger2Args = args;
      return Promise.resolve(pluginStackItem);
    });
    class TestPlugin1 extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger1;
      }
    }
    class TestPlugin2 extends FileListPlugin {
      constructor() {
        super([]);
        this.onFileTrigger = onFileTrigger2;
      }
    }
    const handler = FileListPluginHandler([
      new TestPlugin1(),
      new TestPlugin2(),
      new TestPlugin1(), //noop
    ]);
    let readNextBytesArgs = /** @type {any[]} */ ([]);
    const readNextBytes = mockFunction((...args) => {
      readNextBytesArgs = args;
      return Promise.resolve(123);
    });
    const close = mockFunction(() => {
      return Promise.resolve();
    });
    const source = new MockFileSource({ readNextBytes, close });
    let readFileArgs = /** @type {any[]} */ ([]);
    const readFile = mockFunction((...args) => {
      readFileArgs = args;
      return Promise.resolve(source);
    });
    const actions = new MockFileListActions({
      api: new MockFileListApi({ readFile }),
    });
    const item = FileListItem("file 1");
    const state = {
      ...FileListState(),
      currDir: FileListDir("/sub-dir", false, [item]),
    };
    let stackData = [new PanelStackItem(fsComp, dispatch, actions, state)];
    const updater = mockFunction((f) => {
      stackData = f(stackData);
    });
    const stack = new PanelStack(true, stackData, updater);
    assert.deepEqual(stack.peek().component === fsComp, true);
    const filePath = path.join(state.currDir.path, item.name);

    //when
    handler.openCurrItem(dispatch, stack);

    //then
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(readFile.times, 1);
    assert.deepEqual(readFileArgs, [state.currDir.path, item, 0]);
    assert.deepEqual(readNextBytes.times, 1);
    /** @type {Uint8Array} */
    const buff = readNextBytesArgs[0];
    assert.deepEqual(buff.length, 64 * 1024);
    assert.deepEqual(close.times, 1);
    assert.deepEqual(onFileTrigger1.times, 1);
    assert.deepEqual(onFileTrigger1Args.slice(0, 1), [filePath]);
    /** @type {Uint8Array} */
    const fileHeader = onFileTrigger1Args[1];
    assert.deepEqual(fileHeader.length, 123);
    assert.deepEqual(onFileTrigger2.times, 1);
    assert.deepEqual(onFileTrigger2Args.slice(0, 1), [filePath]);
    assert.deepEqual(onFileTrigger2Args[1] === fileHeader, true);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(updater.times, 1);
    assert.deepEqual(stackData.length, 2);
    const currStackItem = stackData[0];
    assert.deepEqual(currStackItem.component === pluginComp, true);
    /** @type {() => void} */
    const closePlugin = onFileTrigger2Args[2];

    //when
    closePlugin();

    //then
    assert.deepEqual(updater.times, 2);
    assert.deepEqual(stackData.length, 1);
    const currStackItem2 = stackData[0];
    assert.deepEqual(currStackItem2.component === fsComp, true);
  });

  it("should return undefined if not triggerKey when openPluginUi", async () => {
    //given
    const dispatch = mockFunction();
    const onKeyTrigger = mockFunction();
    const keyFull = "C-p";
    class TestPlugin extends FileListPlugin {
      constructor() {
        super(["test"]);
        this.onKeyTrigger = onKeyTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const key = /** @type {IKeyEventArg} */ ({
      name: "",
      full: keyFull,
      data: {},
    });

    //when
    const result = await handler.openPluginUi(dispatch, key, stacks);

    //then
    assert.deepEqual(result === undefined, true);
    assert.deepEqual(onKeyTrigger.times, 0);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should dispatch error task if failed when openPluginUi", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const error = Error("test error");
    let onKeyTriggerArgs = /** @type {any[]} */ ([]);
    const onKeyTrigger = mockFunction((...args) => {
      onKeyTriggerArgs = args;
      return Promise.reject(error);
    });
    const keyFull = "C-p";
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([keyFull]);
        this.onKeyTrigger = onKeyTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const key = /** @type {IKeyEventArg} */ ({
      name: "",
      full: keyFull,
      data: {},
    });

    //when
    const result = await handler.openPluginUi(dispatch, key, stacks);

    //then
    assert.deepEqual(result === undefined, true);
    assert.deepEqual(onKeyTrigger.times, 1);
    assert.deepEqual(onKeyTriggerArgs, [keyFull, stacks, key.data]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Opening Plugin Ui");
    let capturedError = null;
    try {
      await action.task.result;
    } catch (err) {
      capturedError = err;
    }
    assert.deepEqual(capturedError, error);
  });

  it("should return ui component if successful when openPluginUi", async () => {
    //given
    const dispatch = mockFunction();
    const uiComp = () => null;
    let onKeyTriggerArgs = /** @type {any[]} */ ([]);
    const onKeyTrigger = mockFunction((...args) => {
      onKeyTriggerArgs = args;
      return Promise.resolve(uiComp);
    });
    const keyFull = "C-p";
    class TestPlugin extends FileListPlugin {
      constructor() {
        super([keyFull]);
        this.onKeyTrigger = onKeyTrigger;
      }
    }
    const handler = FileListPluginHandler([new TestPlugin()]);
    const key = /** @type {IKeyEventArg} */ ({
      name: "",
      full: keyFull,
      data: {},
    });

    //when
    const result = await handler.openPluginUi(dispatch, key, stacks);

    //then
    assert.deepEqual(result === uiComp, true);
    assert.deepEqual(onKeyTrigger.times, 1);
    assert.deepEqual(onKeyTriggerArgs, [keyFull, stacks, key.data]);
    assert.deepEqual(dispatch.times, 0);
  });
});
