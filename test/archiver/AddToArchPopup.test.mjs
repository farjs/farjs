/**
 * @import { ButtonsPanelAction } from "@farjs/ui/ButtonsPanel.mjs"
 * @import { AddToArchPopupProps } from "../../archiver/AddToArchPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextBox from "@farjs/ui/TextBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import AddToArchPopup from "../../archiver/AddToArchPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

AddToArchPopup.modalComp = mockComponent(Modal);
AddToArchPopup.textLineComp = mockComponent(TextLine);
AddToArchPopup.textBoxComp = mockComponent(TextBox);
AddToArchPopup.horizontalLineComp = mockComponent(HorizontalLine);
AddToArchPopup.buttonsPanelComp = mockComponent(ButtonsPanel);

const {
  modalComp,
  textLineComp,
  textBoxComp,
  horizontalLineComp,
  buttonsPanelComp,
} = AddToArchPopup;

describe("AddToArchPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", () => {
    //given
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onCancel });
    const comp = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    ).root;
    const modalProps = comp.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should set zipName when onChange in TextBox", () => {
    //given
    const zipName = "initial zip name";
    const props = getAddToArchPopupProps({ zipName });
    const renderer = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    );
    const textBoxProps = renderer.root.findByType(textBoxComp).props;
    assert.deepEqual(textBoxProps.value, zipName);
    const newZipName = "new zip name";

    //when
    textBoxProps.onChange(newZipName);

    //then
    const updatedProps = renderer.root.findByType(textBoxComp).props;
    assert.deepEqual(updatedProps.value, newZipName);
  });

  it("should call onAction when onEnter in TextBox", () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onAction, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    );
    const textBoxProps = renderer.root.findByType(textBoxComp).props;
    const newZipName = "new zip name";
    textBoxProps.onChange(newZipName);
    const updatedProps = renderer.root.findByType(textBoxComp).props;

    //when
    updatedProps.onEnter();

    //then
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, [newZipName]);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should call onAction when press Action button", () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onAction, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    );
    const textBoxProps = renderer.root.findByType(textBoxComp).props;
    const newZipName = "new zip name";
    textBoxProps.onChange(newZipName);
    /** @type {readonly ButtonsPanelAction[]} */
    const actions = renderer.root.findByType(buttonsPanelComp).props.actions;

    //when
    actions[0].onAction();

    //then
    assert.deepEqual(onAction.times, 1);
    assert.deepEqual(onActionArgs, [newZipName]);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should not call onAction if zipName is empty", () => {
    //given
    const onAction = mockFunction();
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onAction, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    );
    const textBoxProps = renderer.root.findByType(textBoxComp).props;
    const newZipName = "";
    textBoxProps.onChange(newZipName);
    /** @type {readonly ButtonsPanelAction[]} */
    const actions = renderer.root.findByType(buttonsPanelComp).props.actions;

    //when
    actions[0].onAction();

    //then
    assert.deepEqual(onAction.times, 0);
    assert.deepEqual(onCancel.times, 0);
  });

  it("should call onCancel when press Cancel button", () => {
    //given
    const onAction = mockFunction();
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onAction, onCancel });
    const renderer = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    );
    /** @type {readonly ButtonsPanelAction[]} */
    const actions = renderer.root.findByType(buttonsPanelComp).props.actions;

    //when
    actions[1].onAction();

    //then
    assert.deepEqual(onAction.times, 0);
    assert.deepEqual(onCancel.times, 1);
  });

  it("should render component", () => {
    //given
    const onCancel = mockFunction();
    const props = getAddToArchPopupProps({ onCancel });

    //when
    const result = TestRenderer.create(
      withThemeContext(h(AddToArchPopup, props))
    ).root;

    //then
    assertAddToArchPopup(result, props, ["[ Add ]", "[ Cancel ]"]);
  });
});

/**
 * @param {Partial<AddToArchPopupProps>} props
 * @returns {AddToArchPopupProps}
 */
function getAddToArchPopupProps(props = {}) {
  return {
    zipName: "test zipName",
    action: "Add",
    onAction: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {AddToArchPopupProps} props
 * @param {readonly string[]} actions
 */
function assertAddToArchPopup(result, props, actions) {
  assert.deepEqual(AddToArchPopup.displayName, "AddToArchPopup");

  const [width, height] = [75, 8];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = DefaultTheme.popup.regular;

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: "Add files to archive",
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
        text: "Add to zip archive:",
        style: theme,
        padding: 0,
      }),
      h(textBoxComp, {
        left: contentLeft,
        top: 2,
        width: contentWidth,
        value: props.zipName,
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
