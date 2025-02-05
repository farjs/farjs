/**
 * @typedef {import("../../../file/popups/TextSearchPopup.mjs").TextSearchPopupProps} TextSearchPopupProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Modal from "@farjs/ui/popup/Modal.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import TextSearchPopup from "../../../file/popups/TextSearchPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

TextSearchPopup.modalComp = mockComponent(Modal);
TextSearchPopup.textLineComp = mockComponent(TextLine);
TextSearchPopup.comboBoxComp = mockComponent(ComboBox);
TextSearchPopup.horizontalLineComp = mockComponent(HorizontalLine);
TextSearchPopup.buttonsPanelComp = mockComponent(ButtonsPanel);

const {
  modalComp,
  textLineComp,
  comboBoxComp,
  horizontalLineComp,
  buttonsPanelComp,
} = TextSearchPopup;

describe("TextSearchPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", () => {
    //given
    const onCancel = mockFunction();
    const props = getTextSearchPopupProps({ onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const modalProps = renderer.root.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should set searchText when onChange in ComboBox", () => {
    //given
    const props = getTextSearchPopupProps();
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(comboBoxProps.value, "");
    const newSearchText = "new searchText";

    //when
    comboBoxProps.onChange(newSearchText);

    //then
    const updatedProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(updatedProps.value, newSearchText);
  });

  it("should call onSearch when onEnter in ComboBox", () => {
    //given
    let onSearchArgs = /** @type {any[]} */ ([]);
    const onSearch = mockFunction((...args) => (onSearchArgs = args));
    const onCancel = mockFunction();
    const props = getTextSearchPopupProps({ onSearch, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    comboBoxProps.onChange("test");
    const updatedProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(updatedProps.value, "test");

    //when
    updatedProps.onEnter();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onSearch.times, 1);
    assert.deepEqual(onSearchArgs, ["test"]);
  });

  it("should call onSearch when press OK button", () => {
    //given
    let onSearchArgs = /** @type {any[]} */ ([]);
    const onSearch = mockFunction((...args) => (onSearchArgs = args));
    const onCancel = mockFunction();
    const props = getTextSearchPopupProps({ onSearch, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    comboBoxProps.onChange("test");
    const updatedProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(updatedProps.value, "test");
    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[0].onAction();

    //then
    assert.deepEqual(onCancel.times, 0);
    assert.deepEqual(onSearch.times, 1);
    assert.deepEqual(onSearchArgs, ["test"]);
  });

  it("should not call onSearch if searchText is empty", () => {
    //given
    const onSearch = mockFunction();
    const onCancel = mockFunction();
    const props = getTextSearchPopupProps({ onSearch, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const comboBoxProps = renderer.root.findByType(comboBoxComp).props;
    assert.deepEqual(comboBoxProps.value, "");

    //when
    comboBoxProps.onEnter();

    //then
    assert.deepEqual(onSearch.times, 0);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should call onCancel when press Cancel button", () => {
    //given
    const onCancel = mockFunction();
    const props = getTextSearchPopupProps({ onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    );
    const buttonsProps = renderer.root.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[1].onAction();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should render component", () => {
    //given
    const props = getTextSearchPopupProps();

    //when
    const result = TestRenderer.create(
      withThemeContext(h(TextSearchPopup, props))
    ).root;

    //then
    assertTextSearchPopup(result);
  });
});

/**
 * @param {Partial<TextSearchPopupProps>} props
 * @returns {TextSearchPopupProps}
 */
function getTextSearchPopupProps(props = {}) {
  return {
    onSearch: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertTextSearchPopup(result) {
  assert.deepEqual(TextSearchPopup.displayName, "TextSearchPopup");

  const width = 75;
  const height = 8;
  const style = FileListTheme.defaultTheme.popup.regular;

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: "Search",
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
        text: "Search for",
        style,
        padding: 0,
      }),
      h(comboBoxComp, {
        left: 2,
        top: 2,
        width: width - 10,
        items: [],
        value: "",
        onChange: mockFunction(),
        onEnter: mockFunction(),
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
      h(buttonsPanelComp, {
        top: 4,
        actions: [
          { label: "[ Search ]", onAction: mockFunction() },
          { label: "[ Cancel ]", onAction: mockFunction() },
        ],
        style,
        margin: 2,
      })
    )
  );
}
