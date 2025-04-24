/**
 * @import { FolderShortcutsPopupProps } from "../../../fs/popups/FolderShortcutsPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import withServicesContext from "../withServicesContext.mjs";
import MockFolderShortcutsService from "../../../fs/popups/MockFolderShortcutsService.mjs";
import FolderShortcutsPopup from "../../../fs/popups/FolderShortcutsPopup.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FolderShortcutsPopup.listPopup = mockComponent(ListPopup);

const { listPopup } = FolderShortcutsPopup;

const otherComp = () => null;
const fsComp = () => null;

describe("FolderShortcutsPopup.test.mjs", () => {
  it("should call onChangeDir when onAction", async () => {
    //given
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const props = getFolderShortcutsPopupProps({ onChangeDir });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(
      Array(10)
        .fill("item")
        .map((_, idx) => `${_}${idx}`)
    );
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    const popupProps = comp.findByType(listPopup).props;

    //when
    popupProps.onAction(0);

    //then
    assert.deepEqual(getAll.times, 1);
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, ["item0"]);
  });

  it("should not call onChangeDir if <none> when onAction", async () => {
    //given
    const onChangeDir = mockFunction();
    const props = getFolderShortcutsPopupProps({ onChangeDir });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(Array(10).fill(undefined));
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    const popupProps = comp.findByType(listPopup).props;

    //when
    popupProps.onAction(0);

    //then
    assert.deepEqual(getAll.times, 1);
    assert.deepEqual(onChangeDir.times, 0);
  });

  it("should call onChangeDir when onKeypress(0-9)", async () => {
    //given
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const props = getFolderShortcutsPopupProps({ onChangeDir });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(
      Array(10)
        .fill("item")
        .map((_, idx) => `${_} ${idx + 1}`)
    );
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    const popupProps = comp.findByType(listPopup).props;

    /** @type {(index: number) => void} */
    function check(index) {
      const onChangeDirTimes = onChangeDir.times;

      //when
      const result = popupProps.onKeypress(`${index}`);

      //then
      assert.deepEqual(result, true);
      assert.deepEqual(onChangeDir.times, onChangeDirTimes + 1);
      assert.deepEqual(onChangeDirArgs, [`item ${index + 1}`]);
    }

    //when & then
    check(0);
    check(1);
    check(2);
    check(3);
    check(4);
    check(5);
    check(6);
    check(7);
    check(8);
    check(9);
  });

  it("should set item to <none> when onKeypress(-)", async () => {
    //given
    const props = getFolderShortcutsPopupProps();
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(
      Array(10)
        .fill("item")
        .map((_, idx) => `${_} ${idx + 1}`)
    );
    const deleteP = Promise.resolve();
    const getAll = mockFunction(() => itemsP);
    let deleteArgs = /** @type {any[]} */ ([]);
    const deleteMock = mockFunction((...args) => {
      deleteArgs = args;
      return deleteP;
    });
    const shortcutsService = new MockFolderShortcutsService({
      getAll,
      delete: deleteMock,
    });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    comp.findByType(listPopup).props.onSelect(1);
    const popupProps = comp.findByType(listPopup).props;
    assert.deepEqual(popupProps.items[1], "1: item 2");

    //when
    const result = popupProps.onKeypress("-");

    //then
    await deleteP;
    assert.deepEqual(result, true);
    assert.deepEqual(deleteMock.times, 1);
    assert.deepEqual(deleteArgs, [1]);
    assert.deepEqual(comp.findByType(listPopup).props.items[1], "1: <none>");
  });

  it("should set item to current fs path when onKeypress(+)", async () => {
    //given
    const props = getFolderShortcutsPopupProps();
    const currState = {
      ...FileListState(),
      currDir: FileListDir("/test", false, []),
    };
    const stacksProps = WithStacksProps(
      WithStacksData(
        new PanelStack(
          true,
          [
            new PanelStackItem(otherComp),
            new PanelStackItem(fsComp, undefined, undefined, currState),
          ],
          mockFunction()
        )
      ),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(Array(10).fill(undefined));
    const saveP = Promise.resolve();
    const getAll = mockFunction(() => itemsP);
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const shortcutsService = new MockFolderShortcutsService({
      getAll,
      save,
    });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    comp.findByType(listPopup).props.onSelect(1);
    const popupProps = comp.findByType(listPopup).props;
    assert.deepEqual(popupProps.items[1], "1: <none>");

    //when
    const result = popupProps.onKeypress("+");

    //then
    await saveP;
    assert.deepEqual(result, true);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [1, "/test"]);
    assert.deepEqual(comp.findByType(listPopup).props.items[1], "1: /test");
  });

  it("should return false if unknown key when onKeypress", async () => {
    //given
    const props = getFolderShortcutsPopupProps();
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(Array(10).fill("item"));
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    const popupProps = comp.findByType(listPopup).props;

    //when
    const result = popupProps.onKeypress("unknown");

    //then
    assert.deepEqual(result, false);
  });

  it("should call onClose when onClose", async () => {
    //given
    const onClose = mockFunction();
    const props = getFolderShortcutsPopupProps({ onClose });
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve(Array(10).fill("item"));
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });
    const comp = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    const popupProps = comp.findByType(listPopup).props;

    //when
    popupProps.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup", async () => {
    //given
    const props = getFolderShortcutsPopupProps();
    const stacksProps = WithStacksProps(
      WithStacksData(new PanelStack(true, [], mockFunction())),
      WithStacksData(new PanelStack(false, [], mockFunction()))
    );
    const itemsP = Promise.resolve([
      "item",
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
    ]);
    const getAll = mockFunction(() => itemsP);
    const shortcutsService = new MockFolderShortcutsService({ getAll });

    //when
    const result = TestRenderer.create(
      withStacksContext(
        withServicesContext(h(FolderShortcutsPopup, props), shortcutsService),
        stacksProps
      )
    ).root;

    //then
    await itemsP;
    assert.deepEqual(getAll.times, 1);
    assertFolderShortcutsPopup(result, [
      "0: item",
      "1: <none>",
      "2: <none>",
      "3: <none>",
      "4: <none>",
      "5: <none>",
      "6: <none>",
      "7: <none>",
      "8: <none>",
      "9: <none>",
    ]);
  });
});

/**
 * @param {Partial<FolderShortcutsPopupProps>} props
 * @returns {FolderShortcutsPopupProps}
 */
function getFolderShortcutsPopupProps(props = {}) {
  return {
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {string[]} items
 */
function assertFolderShortcutsPopup(result, items) {
  assert.deepEqual(FolderShortcutsPopup.displayName, "FolderShortcutsPopup");

  assertComponents(
    result.children,
    h(listPopup, {
      title: "Folder shortcuts",
      items: items,
      footer: "Edit: +, -",
      onAction: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
