/**
 * @import { History } from "@farjs/filelist/history/HistoryProvider.mjs"
 * @import { CopyItemsPopupProps } from "../../copymove/CopyItemsPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import CopyItemsPopup from "../../copymove/CopyItemsPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

CopyItemsPopup.modalComp = mockComponent(Modal);
CopyItemsPopup.textLineComp = mockComponent(TextLine);
CopyItemsPopup.comboBoxComp = mockComponent(ComboBox);
CopyItemsPopup.horizontalLineComp = mockComponent(HorizontalLine);
CopyItemsPopup.buttonsPanelComp = mockComponent(ButtonsPanel);

const {
  modalComp,
  textLineComp,
  comboBoxComp,
  horizontalLineComp,
  buttonsPanelComp,
} = CopyItemsPopup;

describe("CopyItemsPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", async () => {
    //given
    const onCancel = mockFunction();
    const props = getCopyItemsPopupProps({ onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {readonly History[]} */
    const historyItems = [];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const comp = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(
            withThemeContext(h(CopyItemsPopup, props)),
            provider
          )
        );
      })
    ).root;
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const modalProps = comp.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should set path when onChange in TextBox", async () => {
    //given
    const path = "initial path";
    const props = getCopyItemsPopupProps({ path });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(CopyItemsPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBox = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(comboBox.value, path);
    const newFolderName = "new path";

    //when
    TestRenderer.act(() => {
      comboBox.onChange(newFolderName);
    });

    //then
    assert.deepEqual(
      renderer.root.findByType(comboBoxComp).props.value,
      newFolderName
    );
  });

  it("should call onAction when onEnter in TextBox", async () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onCancel = mockFunction();
    const props = getCopyItemsPopupProps({ path: "test", onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(CopyItemsPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBox = renderer.root.findByType(comboBoxComp).props;

    //when
    comboBox.onEnter();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, ["test"]);
  });

  it("should call onAction when press action button", async () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onCancel = mockFunction();
    const props = getCopyItemsPopupProps({ path: "test", onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(CopyItemsPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;
    const action = buttonsProps.actions[0];

    //when
    action.onAction();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, ["test"]);
  });

  it("should not call onAction if path is empty", async () => {
    //given
    const onAction = mockFunction();
    const onCancel = mockFunction();
    const props = getCopyItemsPopupProps({ path: "", onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(CopyItemsPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;
    const action = buttonsProps.actions[0];

    //when
    action.onAction();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onAction.times, 0);
  });

  it("should call onCancel when press Cancel button", async () => {
    //given
    const onAction = mockFunction();
    const onCancel = mockFunction();
    const props = getCopyItemsPopupProps({ path: "", onAction, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);
    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(CopyItemsPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;
    const action = buttonsProps.actions[1];

    //when
    action.onAction();

    //then
    assert.deepEqual(onCancel.times, 1);
    assert.deepEqual(onAction.times, 0);
  });

  it("should render component when copy single item", async () => {
    //given
    const props = getCopyItemsPopupProps({
      move: false,
      items: [FileListItem("file 1")],
    });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(
            withThemeContext(h(CopyItemsPopup, props)),
            provider
          )
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertCopyItemsPopup(result, props, items, ["[ Copy ]", "[ Cancel ]"]);
  });

  it("should render component when copy multiple items", async () => {
    //given
    const props = getCopyItemsPopupProps({
      move: false,
      items: [FileListItem("file 1"), FileListItem("file 2")],
    });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    const items = ["path", "path 2"];
    /** @type {readonly History[]} */
    const historyItems = items.map((_) => {
      return { item: _ };
    });
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(
            withThemeContext(h(CopyItemsPopup, props)),
            provider
          )
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertCopyItemsPopup(result, props, items, ["[ Copy ]", "[ Cancel ]"]);
  });

  it("should render component when move", async () => {
    //given
    const props = getCopyItemsPopupProps({ move: true, items: [] });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {readonly History[]} */
    const historyItems = [];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(historyItems);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(
            withThemeContext(h(CopyItemsPopup, props)),
            provider
          )
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [CopyItemsPopup.copyItemsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertCopyItemsPopup(result, props, [], ["[ Rename ]", "[ Cancel ]"]);
  });
});

/**
 * @param {Partial<CopyItemsPopupProps>} props
 * @returns {CopyItemsPopupProps}
 */
function getCopyItemsPopupProps(props = {}) {
  return {
    move: false,
    path: "test folder",
    items: [FileListItem("file 1")],
    onAction: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {CopyItemsPopupProps} props
 * @param {readonly string[]} items
 * @param {readonly string[]} actions
 */
function assertCopyItemsPopup(result, props, items, actions) {
  assert.deepEqual(CopyItemsPopup.displayName, "CopyItemsPopup");

  const [width, height] = [75, 8];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = DefaultTheme.popup.regular;

  const count = props.items.length;
  const maybeFirstItem = count > 0 ? props.items[0] : undefined;
  const itemsText =
    count > 1
      ? `${count} items`
      : maybeFirstItem !== undefined
      ? `"${maybeFirstItem.name}"`
      : "";
  const title = props.move ? "Rename/Move" : "Copy";
  const text = props.move ? "Rename or move" : "Copy";

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title,
        width: width,
        height: height,
        style: theme,
        onCancel: mockFunction(),
      },
      h(textLineComp, {
        align: TextAlign.left,
        left: contentLeft,
        top: 1,
        width: contentWidth,
        text: `${text} ${itemsText} to:`,
        style: theme,
        padding: 0,
      }),
      h(comboBoxComp, {
        left: contentLeft,
        top: 2,
        width: contentWidth,
        items: [...items].reverse(),
        value: props.path,
        onChange: mockFunction(),
        onEnter: mockFunction(),
      }),

      h(horizontalLineComp, {
        left: 0,
        top: 3,
        length: width - ModalContent.paddingHorizontal * 2,
        lineCh: SingleChars.horizontal,
        style: theme,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(buttonsPanelComp, {
        top: 4,
        actions: actions.map((_) => {
          return { label: _, onAction: mockFunction() };
        }),
        style: theme,
        margin: 2,
      })
    )
  );
}
