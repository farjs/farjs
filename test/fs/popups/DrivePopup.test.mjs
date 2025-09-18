/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 * @import { FSDisk } from "../../../fs/FSDisk.mjs"
 * @import { DrivePopupProps } from "../../../fs/popups/DrivePopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MenuPopup from "@farjs/ui/menu/MenuPopup.mjs";
import WithSize from "@farjs/ui/WithSize.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import MockFSService from "../../../fs/MockFSService.mjs";
import DrivePopup from "../../../fs/popups/DrivePopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

DrivePopup.withSizeComp = mockComponent(WithSize);
DrivePopup.menuPopup = mockComponent(MenuPopup);

const { withSizeComp, menuPopup, _toCompact } = DrivePopup;

const fsComp = () => null;

describe("DrivePopup.test.mjs", () => {
  it("should call onChangeDir(curr panel path) when onSelect", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "C:", size: 156595318784, free: 81697124352, name: "SYSTEM" },
      { root: "D:", size: 842915639296, free: 352966430720, name: "DATA" },
      { root: "E:", size: 0, free: 0, name: "" },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const props = getDrivePopupProps({
      dispatch,
      showOnLeft: true,
      onChangeDir,
    });
    const currState = {
      ...FileListState(),
      currDir: FileListDir("C:/test", false, []),
    };
    const currStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, undefined, currState)],
      mockFunction()
    );
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(currStack),
      WithStacksData(otherStack)
    );
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(renderer.root.children.length, 1);
    const renderContent = renderer.root
      .findByType(withSizeComp)
      .props.render(60, 20);
    const resultContent = TestRenderer.create(renderContent).root;
    const menuProps = resultContent.findByType(menuPopup).props;

    //when
    menuProps.onSelect(0);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, ["C:/test"]);
  });

  it("should call onChangeDir(other panel path) when onSelect", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "C:", size: 156595318784, free: 81697124352, name: "SYSTEM" },
      { root: "D:", size: 842915639296, free: 352966430720, name: "DATA" },
      { root: "E:", size: 0, free: 0, name: "" },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const props = getDrivePopupProps({
      dispatch,
      showOnLeft: false,
      onChangeDir,
    });
    const currState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const currStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, undefined, currState)],
      mockFunction()
    );
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("C:/test", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(otherStack),
      WithStacksData(currStack)
    );
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(renderer.root.children.length, 1);
    const renderContent = renderer.root
      .findByType(withSizeComp)
      .props.render(60, 20);
    const resultContent = TestRenderer.create(renderContent).root;
    const menuProps = resultContent.findByType(menuPopup).props;

    //when
    menuProps.onSelect(0);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, ["C:/test"]);
  });

  it("should call onChangeDir(new root dir) when onSelect", async () => {
    //given
    const dispatch = mockFunction();
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "C:", size: 156595318784, free: 81697124352, name: "SYSTEM" },
      { root: "D:", size: 842915639296, free: 352966430720, name: "DATA" },
      { root: "E:", size: 0, free: 0, name: "" },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const props = getDrivePopupProps({
      dispatch,
      showOnLeft: true,
      onChangeDir,
    });
    const currState = {
      ...FileListState(),
      currDir: FileListDir("/test", false, []),
    };
    const currStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, undefined, currState)],
      mockFunction()
    );
    const otherState = {
      ...FileListState(),
      currDir: FileListDir("/test2", false, []),
    };
    const otherStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, otherState)],
      mockFunction()
    );
    const stacksProps = WithStacksProps(
      WithStacksData(currStack),
      WithStacksData(otherStack)
    );
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(renderer.root.children.length, 1);
    const renderContent = renderer.root
      .findByType(withSizeComp)
      .props.render(60, 20);
    const resultContent = TestRenderer.create(renderContent).root;
    const menuProps = resultContent.findByType(menuPopup).props;

    //when
    menuProps.onSelect(0);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, ["C:"]);
  });

  it("should call onClose when onClose", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "C:", size: 1, free: 2, name: "Test" },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    const onClose = mockFunction();
    const props = getDrivePopupProps({ dispatch, showOnLeft: true, onClose });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const action = dispatchArgs[0];
    assert.deepEqual(action.task.message, "Reading disks");
    await action.task.result;
    assert.deepEqual(renderer.root.children.length, 1);
    const renderContent = renderer.root
      .findByType(withSizeComp)
      .props.render(60, 20);
    const resultContent = TestRenderer.create(renderContent).root;
    const menuProps = resultContent.findByType(menuPopup).props;

    //when
    menuProps.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render empty component", async () => {
    //given
    const dispatch = mockFunction();
    const readDisksP = Promise.resolve([]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    const props = getDrivePopupProps({ dispatch });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });

    //then
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(renderer.root.children.length, 0);
  });

  it("should render component on Windows", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "C:", size: 156595318784, free: 81697124352, name: "SYSTEM" },
      { root: "D:", size: 842915639296, free: 352966430720, name: "DATA" },
      { root: "E:", size: 0, free: 0, name: "" },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "win32";
    DrivePopup.fsService = new MockFSService({ readDisks });
    const props = getDrivePopupProps({ dispatch, showOnLeft: true });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });

    //then
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const action = dispatchArgs[0];
    await action.task.result;

    assertDrivePopup(renderer.root, [
      "  C: │SYSTEM         │149341 M│ 77912 M ",
      "  D: │DATA           │803867 M│336615 M ",
      "  E: │               │        │         ",
    ]);
  });

  it("should render component on Mac OS/Linux", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    /** @type {Promise<FSDisk[]>} */
    const readDisksP = Promise.resolve([
      { root: "/", size: 156595318784, free: 81697124352, name: "/" },
      {
        root: "/Volumes/TestDrive",
        size: 842915639296,
        free: 352966430720,
        name: "TestDrive",
      },
    ]);
    const readDisks = mockFunction(() => readDisksP);
    DrivePopup.platform = "darwin";
    DrivePopup.fsService = new MockFSService({ readDisks });
    const props = getDrivePopupProps({ dispatch, showOnLeft: true });
    const panelInput = /** @type {BlessedElement} */ ({ width: 50 });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction()), panelInput),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );

    //when
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withStacksContext(h(DrivePopup, props), stacksProps)
      );
    });

    //then
    await readDisksP;
    assert.deepEqual(readDisks.times, 1);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<any>} */
    const action = dispatchArgs[0];
    await action.task.result;

    assertDrivePopup(
      renderer.root,
      [
        " /              │149341 M│ 77912 M ",
        " TestDrive      │803867 M│336615 M ",
      ],
      "0%+4"
    );
  });

  it("should convert bytes to compact form when _toCompact", async () => {
    //when & then
    assert.deepEqual(_toCompact(0), "");
    assert.deepEqual(_toCompact(1000 * 1024), "1024000");
    assert.deepEqual(_toCompact(1000 * 1024 + 1), "1000 K");
    assert.deepEqual(_toCompact(1000 * 1024 * 1024), "1024000 K");
    assert.deepEqual(_toCompact(1000 * 1024 * 1024 + 1), "1000 M");
    assert.deepEqual(_toCompact(1000 * 1024 * 1024 * 1024), "1024000 M");
    assert.deepEqual(_toCompact(1000 * 1024 * 1024 * 1024 + 1), "1000 G");
  });
});

/**
 * @param {Partial<DrivePopupProps>} props
 * @returns {DrivePopupProps}
 */
function getDrivePopupProps(props = {}) {
  return {
    dispatch: mockFunction(),
    showOnLeft: false,
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {string[]} expectedItems
 * @param {string} [expectedLeft]
 */
function assertDrivePopup(result, expectedItems, expectedLeft = "0%+0") {
  assert.deepEqual(DrivePopup.displayName, "DrivePopup");

  const textWidth = expectedItems
    .map((_) => _.length)
    .reduce((res, len) => (res > len ? res : len), 0);
  const width = textWidth + 3 * 2;

  assert.deepEqual(result.children.length, 1);

  const render = result.findByType(withSizeComp).props.render;
  const content = TestRenderer.create(render(60, 20)).root;
  const menuProps = content.findByType(menuPopup).props;
  assert.deepEqual(menuProps.getLeft(width), expectedLeft);

  assertComponents(
    [content],
    h(menuPopup, {
      title: "Drive",
      items: expectedItems,
      getLeft: mockFunction(),
      onSelect: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
