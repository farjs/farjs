/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("../../../filelist/popups/SelectPopup.mjs").SelectPopupProps} SelectPopupProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import SelectPopup from "../../../filelist/popups/SelectPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

SelectPopup.modalComp = mockComponent(Modal);
SelectPopup.comboBoxComp = mockComponent(ComboBox);

const { modalComp, comboBoxComp } = SelectPopup;

describe("SelectPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", async () => {
    //given
    const onCancel = mockFunction();
    const props = getSelectPopupProps({ onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve([]);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const modalProps = renderer.root.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should set pattern when onChange in ComboBox", async () => {
    //given
    const pattern = "initial pattern";
    const props = getSelectPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "test pattern" }, { item: pattern }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(comboBoxProps.value, pattern);
    const newPattern = "new pattern";

    //when
    TestRenderer.act(() => {
      comboBoxProps.onChange(newPattern);
    });

    //then
    const updatedProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(updatedProps.value, newPattern);
  });

  it("should call onAction when onEnter in ComboBox", async () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onCancel = mockFunction();
    const props = getSelectPopupProps({ showSelect: true, onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "pattern" }, { item: "test" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;

    //when
    comboBoxProps.onEnter();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, ["test"]);
  });

  it("should not call onAction if pattern is empty", async () => {
    //given
    const onAction = mockFunction();
    const onCancel = mockFunction();
    const props = getSelectPopupProps({ showSelect: true, onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve([]);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;

    //when
    comboBoxProps.onEnter();

    //then
    assert.deepEqual(onAction.times, 0);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should render Select component", async () => {
    //given
    const props = { ...getSelectPopupProps(), showSelect: true };
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "pattern" }, { item: "pattern 2" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertSelectPopup(result, items, "Select");
  });

  it("should render Deselect component", async () => {
    //given
    const props = { ...getSelectPopupProps(), showSelect: false };
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "pattern" }, { item: "pattern 2" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(withThemeContext(h(SelectPopup, props)), provider)
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertSelectPopup(result, items, "Deselect");
  });
});

/**
 * @param {Partial<SelectPopupProps>} props
 * @returns {SelectPopupProps}
 */
function getSelectPopupProps(props = {}) {
  return {
    showSelect: false,
    onAction: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {History[]} items
 * @param {string} expectedTitle
 */
function assertSelectPopup(result, items, expectedTitle) {
  assert.deepEqual(SelectPopup.displayName, "SelectPopup");

  const width = 55;
  const height = 5;
  const style = FileListTheme.defaultTheme.popup.regular;
  const itemsReversed = [...items].reverse().map((_) => _.item);

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: expectedTitle,
        width,
        height,
        style,
        onCancel: mockFunction(),
      },
      h(comboBoxComp, {
        left: 2,
        top: 1,
        width: width - 10,
        items: itemsReversed,
        value: itemsReversed[0],
        onChange: mockFunction(),
      })
    )
  );
}
