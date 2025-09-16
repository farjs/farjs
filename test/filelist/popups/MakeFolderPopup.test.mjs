/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("../../../filelist/popups/MakeFolderPopup.mjs").MakeFolderPopupProps} MakeFolderPopupProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Modal from "@farjs/ui/popup/Modal.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import CheckBox from "@farjs/ui/CheckBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import MakeFolderPopup from "../../../filelist/popups/MakeFolderPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

MakeFolderPopup.modalComp = mockComponent(Modal);
MakeFolderPopup.textLineComp = mockComponent(TextLine);
MakeFolderPopup.comboBoxComp = mockComponent(ComboBox);
MakeFolderPopup.horizontalLineComp = mockComponent(HorizontalLine);
MakeFolderPopup.checkBoxComp = mockComponent(CheckBox);
MakeFolderPopup.buttonsPanelComp = mockComponent(ButtonsPanel);

const {
  modalComp,
  textLineComp,
  comboBoxComp,
  horizontalLineComp,
  checkBoxComp,
  buttonsPanelComp,
} = MakeFolderPopup;

describe("MakeFolderPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", async () => {
    //given
    const onCancel = mockFunction();
    const props = getMakeFolderPopupProps({ onCancel });
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
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const modalProps = renderer.root.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should set folderName when onChange in ComboBox", async () => {
    //given
    const folderName = "initial folder name";
    const props = getMakeFolderPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "folder" }, { item: folderName }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(comboBoxProps.value, folderName);
    const newFolderName = "new folder name";

    //when
    TestRenderer.act(() => {
      comboBoxProps.onChange(newFolderName);
    });

    //then
    const updatedProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(updatedProps.value, newFolderName);
  });

  it("should set multiple flag when onChange in CheckBox", async () => {
    //given
    const props = getMakeFolderPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "folder" }, { item: "folder 2" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const checkBoxProps = renderer.root.findByType(checkBoxComp).props;
    assert.deepEqual(checkBoxProps.value, false);

    //when
    TestRenderer.act(() => {
      checkBoxProps.onChange();
    });

    //then
    const updatedProps = renderer.root.findByType(checkBoxComp).props;
    assert.deepEqual(updatedProps.value, true);
  });

  it("should call onOk when onEnter in ComboBox", async () => {
    //given
    let onOkArgs = /** @type {any[]} */ ([]);
    const onOk = mockFunction((...args) => (onOkArgs = args));
    const onCancel = mockFunction();
    const props = getMakeFolderPopupProps({ multiple: true, onOk, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "folder" }, { item: "test" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;

    //when
    comboBoxProps.onEnter();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onOk.times, 1);
    assert.deepEqual(onOkArgs, ["test", true]);
  });

  it("should call onOk when press OK button", async () => {
    //given
    let onOkArgs = /** @type {any[]} */ ([]);
    const onOk = mockFunction((...args) => (onOkArgs = args));
    const onCancel = mockFunction();
    const props = getMakeFolderPopupProps({ multiple: true, onOk, onCancel });
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "folder" }, { item: "test" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    const renderer = await actAsync(() => {
      return TestRenderer.create(
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[0].onAction();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onOk.times, 1);
    assert.deepEqual(onOkArgs, ["test", true]);
  });

  it("should not call onOk if folderName is empty", async () => {
    //given
    const onOk = mockFunction();
    const onCancel = mockFunction();
    const props = getMakeFolderPopupProps({ multiple: true, onOk, onCancel });
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
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[0].onAction();

    //then
    assert.deepEqual(onOk.times, 0);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should call onCancel when press Cancel button", async () => {
    //given
    const onOk = mockFunction();
    const onCancel = mockFunction();
    const props = getMakeFolderPopupProps({ onOk, onCancel });
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
        withHistoryProvider(
          withThemeContext(h(MakeFolderPopup, props)),
          provider
        )
      );
    });
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[1].onAction();

    //then
    assert.deepEqual(onOk.times, 0);
    assert.deepEqual(onCancel.times, 1);
  });

  it("should render component", async () => {
    //given
    const props = getMakeFolderPopupProps();
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    const getAll = mockFunction(() => getAllP);
    const service = new MockHistoryService({ getAll });
    const provider = new MockHistoryProvider({ get });
    /** @type {History[]} */
    const items = [{ item: "folder" }, { item: "folder 2" }];
    const getP = Promise.resolve(service);
    const getAllP = Promise.resolve(items);

    //when
    const result = (
      await actAsync(() => {
        return TestRenderer.create(
          withHistoryProvider(
            withThemeContext(h(MakeFolderPopup, props)),
            provider
          )
        );
      })
    ).root;

    //then
    await getP;
    await getAllP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [MakeFolderPopup.mkDirsHistoryKind]);
    assert.deepEqual(getAll.times, 1);

    //then
    assertMakeFolderPopup(result, items, ["[ OK ]", "[ Cancel ]"]);
  });
});

/**
 * @param {Partial<MakeFolderPopupProps>} props
 * @returns {MakeFolderPopupProps}
 */
function getMakeFolderPopupProps(props = {}) {
  return {
    multiple: false,
    onOk: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {History[]} items
 * @param {string[]} actions
 */
function assertMakeFolderPopup(result, items, actions) {
  assert.deepEqual(MakeFolderPopup.displayName, "MakeFolderPopup");

  const width = 75;
  const height = 10;
  const style = FileListTheme.defaultTheme.popup.regular;
  const itemsReversed = [...items].reverse().map((_) => _.item);

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: "Make Folder",
        width,
        height,
        style,
        onCancel: mockFunction(),
      },
      h(textLineComp, {
        align: TextAlign.left,
        left: 2,
        top: 1,
        width: width - 10,
        text: "Create the folder",
        style,
        padding: 0,
      }),
      h(comboBoxComp, {
        left: 2,
        top: 2,
        items: itemsReversed,
        width: width - 10,
        value: itemsReversed[0],
        onChange: mockFunction(),
      }),

      h(horizontalLineComp, {
        left: 0,
        top: 3,
        length: width - 6,
        lineCh: SingleChars.horizontal,
        style,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(checkBoxComp, {
        left: 2,
        top: 4,
        value: false,
        label: "Process multiple names",
        style,
        onChange: mockFunction(),
      }),

      h(horizontalLineComp, {
        left: 0,
        top: 5,
        length: width - 6,
        lineCh: SingleChars.horizontal,
        style,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(buttonsPanelComp, {
        top: 6,
        actions: actions.map((_) => {
          return { label: _, onAction: mockFunction() };
        }),
        style,
        margin: 2,
      })
    )
  );
}
