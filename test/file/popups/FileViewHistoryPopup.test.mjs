/**
 * @typedef {import("../../../file/FileViewHistory.mjs").FileViewHistory} FileViewHistory
 * @typedef {import("../../../file/popups/FileViewHistoryPopup.mjs").FileViewHistoryPopupProps} FileViewHistoryPopupProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import FileViewHistory from "../../../file/FileViewHistory.mjs";
import FileViewHistoryPopup from "../../../file/popups/FileViewHistoryPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FileViewHistoryPopup.listPopup = mockComponent(ListPopup);

const { listPopup } = FileViewHistoryPopup;

describe("FileViewHistoryPopup.test.mjs", () => {
  it("should call onAction when onAction", async () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onClose = mockFunction();
    const props = getFileViewHistoryPopupProps({ onAction, onClose });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {FileViewHistory[]} */
    const historyItems = ["item 1", "item 2"].map((path) => {
      return {
        path,
        params: {
          isEdit: false,
          encoding: "utf8",
          position: 0,
        },
      };
    });
    const items = historyItems.map((h) => FileViewHistory.toHistory(h));
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(h(FileViewHistoryPopup, props), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const listPopupProps = renderer.root.findByType(listPopup).props;
    const index = 1;

    //when
    listPopupProps.onAction(index);

    //then
    assert.deepEqual(onClose.times, 0);
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, [historyItems[index]]);
  });

  it("should render popup", async () => {
    //given
    const props = getFileViewHistoryPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {FileViewHistory[]} */
    const historyItems = new Array(20).fill("item").map((path, index) => {
      return {
        path,
        params: {
          isEdit: index % 2 === 0,
          encoding: "utf8",
          position: 0,
        },
      };
    });
    const items = historyItems.map((h) => FileViewHistory.toHistory(h));
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(h(FileViewHistoryPopup, props), provider)
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertFileViewHistoryPopup(result, props, historyItems);
  });
});

/**
 * @param {Partial<FileViewHistoryPopupProps>} props
 * @returns {FileViewHistoryPopupProps}
 */
function getFileViewHistoryPopupProps(props = {}) {
  return {
    onAction: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {FileViewHistoryPopupProps} props
 * @param {FileViewHistory[]} items
 */
function assertFileViewHistoryPopup(result, props, items) {
  assert.deepEqual(FileViewHistoryPopup.displayName, "FileViewHistoryPopup");

  const listPopupProps = result.findByType(listPopup).props;
  assert.deepEqual(listPopupProps.onClose === props.onClose, true);

  assertComponents(
    result.children,
    h(listPopup, {
      title: "File view history",
      items: items.map((item) => {
        const prefix = item.params.isEdit ? "Edit: " : "View: ";
        return `${prefix}${item.path}`;
      }),
      onAction: mockFunction(),
      onClose: listPopupProps.onClose,
      selected: items.length - 1,
      itemWrapPrefixLen: 9,
    })
  );
}
