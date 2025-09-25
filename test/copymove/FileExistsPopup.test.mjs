/**
 * @import { FileExistsAction, FileExistsPopupProps } from "../../copymove/FileExistsPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Modal from "@farjs/ui/popup/Modal.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";
import FileExistsPopup from "../../copymove/FileExistsPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FileExistsPopup.modalComp = mockComponent(Modal);
FileExistsPopup.textLineComp = mockComponent(TextLine);
FileExistsPopup.horizontalLineComp = mockComponent(HorizontalLine);
FileExistsPopup.buttonsPanelComp = mockComponent(ButtonsPanel);

const { modalComp, textLineComp, horizontalLineComp, buttonsPanelComp } =
  FileExistsPopup;

describe("FileExistsPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", () => {
    //given
    const onCancel = mockFunction();
    const props = getFileExistsPopupProps({ onCancel });
    const comp = TestRenderer.create(
      withThemeContext(h(FileExistsPopup, props))
    ).root;
    const modalProps = comp.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should call onAction() when press buttons", () => {
    //given
    let onActionArgs = /** @type {any[]} */ ([]);
    const onAction = mockFunction((...args) => (onActionArgs = args));
    const props = getFileExistsPopupProps({ onAction });
    const comp = TestRenderer.create(
      withThemeContext(h(FileExistsPopup, props))
    ).root;
    const buttonsProps = comp.findByType(buttonsPanelComp).props;

    /** @type {(idx: number, expectedAction: FileExistsAction) => void} */
    function check(idx, expectedAction) {
      //given
      const currOnActionTimes = onAction.times;

      //when
      buttonsProps.actions[idx].onAction();

      //then
      assert.deepEqual(onAction.times, currOnActionTimes + 1);
      assert.deepEqual(onActionArgs, [expectedAction]);
    }

    //when & then
    check(0, "Overwrite");
    check(1, "All");
    check(2, "Skip");
    check(3, "SkipAll");
    check(4, "Append");
  });

  it("should call onCancel when press Cancel button", () => {
    //given
    const onCancel = mockFunction();
    const props = getFileExistsPopupProps({ onCancel });
    const comp = TestRenderer.create(
      withThemeContext(h(FileExistsPopup, props))
    ).root;
    const buttonsProps = comp.findByType(buttonsPanelComp).props;

    //when
    buttonsProps.actions[5].onAction();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should render component", () => {
    //given
    const props = getFileExistsPopupProps();

    //when
    const result = TestRenderer.create(
      withThemeContext(h(FileExistsPopup, props))
    ).root;

    //then
    assertFileExistsPopup(result, props, [
      "Overwrite",
      "All",
      "Skip",
      "Skip all",
      "Append",
      "Cancel",
    ]);
  });
});

/**
 * @param {Partial<FileExistsPopupProps>} props
 * @returns {FileExistsPopupProps}
 */
function getFileExistsPopupProps(props = {}) {
  return {
    newItem: { ...FileListItem("file 1"), size: 1 },
    existing: { ...FileListItem("file 1"), size: 2 },
    onAction: mockFunction(),
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {FileExistsPopupProps} props
 * @param {string[]} actions
 */
function assertFileExistsPopup(result, props, actions) {
  assert.deepEqual(FileExistsPopup.displayName, "FileExistsPopup");

  const [width, height] = [58, 11];
  const theme = DefaultTheme.popup.error;

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: "Warning",
        width: width,
        height: height,
        style: theme,
        onCancel: mockFunction(),
      },
      h("text", {
        left: "center",
        top: 1,
        style: theme,
        content: "File already exists",
      }),
      h(textLineComp, {
        align: TextAlign.center,
        left: 2,
        top: 2,
        width: width - 10,
        text: props.newItem.name,
        style: theme,
        padding: 0,
      }),
      h(horizontalLineComp, {
        left: 0,
        top: 3,
        length: width - 6,
        lineCh: SingleChars.horizontal,
        style: theme,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),

      h("text", {
        left: 2,
        top: 4,
        style: theme,
        content: `New
Existing`,
      }),

      h(textLineComp, {
        align: TextAlign.right,
        left: 2,
        top: 4,
        width: width - 10,
        text: (() => {
          const date = new Date(props.newItem.mtimeMs);
          return `${formatSize(
            props.newItem.size
          )} ${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
        })(),
        style: theme,
        padding: 0,
      }),
      h(textLineComp, {
        align: TextAlign.right,
        left: 2,
        top: 5,
        width: width - 10,
        text: (() => {
          const date = new Date(props.existing.mtimeMs);
          return `${formatSize(
            props.existing.size
          )} ${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
        })(),
        style: theme,
        padding: 0,
      }),

      h(horizontalLineComp, {
        left: 0,
        top: 6,
        length: width - 6,
        lineCh: SingleChars.horizontal,
        style: theme,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(buttonsPanelComp, {
        top: 7,
        actions: actions.map((_) => {
          return { label: _, onAction: mockFunction() };
        }),
        style: theme,
        padding: 1,
      })
    )
  );
}
