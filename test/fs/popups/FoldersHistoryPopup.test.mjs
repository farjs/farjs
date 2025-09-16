/**
 * @import { History } from "@farjs/filelist/history/HistoryProvider.mjs"
 * @import { FoldersHistoryPopupProps } from "../../../fs/popups/FoldersHistoryPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import FSFoldersHistory from "../../../fs/FSFoldersHistory.mjs";
import FoldersHistoryPopup from "../../../fs/popups/FoldersHistoryPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FoldersHistoryPopup.listPopup = mockComponent(ListPopup);

const { listPopup } = FoldersHistoryPopup;

describe("FoldersHistoryPopup.test.mjs", () => {
  it("should call onChangeDir when onAction", async () => {
    //given
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const onClose = mockFunction();
    const props = getFoldersHistoryPopupProps({ onChangeDir, onClose });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "item 1" }, { item: "item 2" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(h(FoldersHistoryPopup, props), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FSFoldersHistory.foldersHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const listPopupProps = renderer.root.findByType(listPopup).props;
    const index = 1;

    //when
    listPopupProps.onAction(index);

    //then
    assert.deepEqual(onClose.times, 0);
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, ["item 2"]);
  });

  it("should render popup", async () => {
    //given
    const props = getFoldersHistoryPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = new Array(20).fill("item");
    const getP = Promise.resolve(service);
    /** @type {History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getAllP = Promise.resolve(historyItems);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(h(FoldersHistoryPopup, props), provider)
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FSFoldersHistory.foldersHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertFoldersHistoryPopup(result, props, items);
  });
});

/**
 * @param {Partial<FoldersHistoryPopupProps>} props
 * @returns {FoldersHistoryPopupProps}
 */
function getFoldersHistoryPopupProps(props = {}) {
  return {
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {FoldersHistoryPopupProps} props
 * @param {string[]} items
 */
function assertFoldersHistoryPopup(result, props, items) {
  assert.deepEqual(FoldersHistoryPopup.displayName, "FoldersHistoryPopup");

  const listPopupProps = result.findByType(listPopup).props;
  assert.deepEqual(listPopupProps.onClose === props.onClose, true);

  assertComponents(
    result.children,
    h(listPopup, {
      title: "Folders history",
      items,
      onAction: mockFunction(),
      onClose: listPopupProps.onClose,
      selected: items.length - 1,
    })
  );
}
