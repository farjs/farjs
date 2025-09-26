/**
 * @import { CopyProgressPopupProps } from "../../copymove/CopyProgressPopup.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import ProgressBar from "@farjs/ui/ProgressBar.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import DefaultTheme from "@farjs/ui/theme/DefaultTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";
import CopyProgressPopup from "../../copymove/CopyProgressPopup.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

CopyProgressPopup.modalComp = mockComponent(Modal);
CopyProgressPopup.textLineComp = mockComponent(TextLine);
CopyProgressPopup.horizontalLineComp = mockComponent(HorizontalLine);
CopyProgressPopup.progressBarComp = mockComponent(ProgressBar);

const {
  modalComp,
  textLineComp,
  horizontalLineComp,
  progressBarComp,
  _toTime,
  _toSpeed,
} = CopyProgressPopup;

describe("CopyProgressPopup.test.mjs", () => {
  it("should call onCancel when onCancel in modal", () => {
    //given
    const onCancel = mockFunction();
    const props = getCopyProgressPopupProps({ onCancel });
    const comp = TestRenderer.create(
      withThemeContext(h(CopyProgressPopup, props))
    ).root;
    const modalProps = comp.findByType(modalComp).props;

    //when
    modalProps.onCancel();

    //then
    assert.deepEqual(onCancel.times, 1);
  });

  it("should render component when copy", () => {
    //given
    const props = getCopyProgressPopupProps({ move: false });

    //when
    const result = TestRenderer.create(
      withThemeContext(h(CopyProgressPopup, props))
    ).root;

    //then
    assertCopyProgressPopup(result, props);
  });

  it("should render component when move", () => {
    //given
    const props = getCopyProgressPopupProps({ move: true });

    //when
    const result = TestRenderer.create(
      withThemeContext(h(CopyProgressPopup, props))
    ).root;

    //then
    assertCopyProgressPopup(result, props);
  });

  it("should convert seconds to time when _toTime", () => {
    //when & then
    assert.deepEqual(_toTime(0), "00:00:00");
    assert.deepEqual(_toTime(1), "00:00:01");
    assert.deepEqual(_toTime(61), "00:01:01");
    assert.deepEqual(_toTime(3601), "01:00:01");
    assert.deepEqual(_toTime(3661), "01:01:01");
    assert.deepEqual(_toTime(3662), "01:01:02");
  });

  it("should convert bits per second to speed when _toSpeed", () => {
    //when & then
    assert.deepEqual(_toSpeed(0), "0b");
    assert.deepEqual(_toSpeed(99000), "99,000b");
    assert.deepEqual(_toSpeed(100000), "100Kb");
    assert.deepEqual(_toSpeed(99000000), "99,000Kb");
    assert.deepEqual(_toSpeed(100000000), "100Mb");
    assert.deepEqual(_toSpeed(99000000000), "99,000Mb");
    assert.deepEqual(_toSpeed(100000000000), "100Gb");
  });
});

/**
 * @param {Partial<CopyProgressPopupProps>} props
 * @returns {CopyProgressPopupProps}
 */
function getCopyProgressPopupProps(props = {}) {
  return {
    move: false,
    item: "test item",
    to: "test to",
    itemPercent: 1,
    total: 2,
    totalPercent: 3,
    timeSeconds: 4,
    leftSeconds: 5,
    bytesPerSecond: 6,
    onCancel: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {CopyProgressPopupProps} props
 */
function assertCopyProgressPopup(result, props) {
  assert.deepEqual(CopyProgressPopup.displayName, "CopyProgressPopup");

  const [width, height] = [50, 13];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = DefaultTheme.popup.regular;

  assertComponents(
    result.children,
    h(
      modalComp,
      {
        title: props.move ? "Move" : "Copy",
        width: width,
        height: height,
        style: theme,
        onCancel: mockFunction(),
      },
      h("text", {
        left: contentLeft,
        top: 1,
        style: theme,
        content: `${props.move ? "Moving" : "Copying"} the file

to
`,
      }),
      h(textLineComp, {
        align: TextAlign.left,
        left: contentLeft,
        top: 2,
        width: contentWidth,
        text: props.item,
        style: theme,
        padding: 0,
      }),
      h(textLineComp, {
        align: TextAlign.left,
        left: contentLeft,
        top: 4,
        width: contentWidth,
        text: props.to,
        style: theme,
        padding: 0,
      }),

      h(progressBarComp, {
        percent: props.itemPercent,
        left: contentLeft,
        top: 5,
        length: contentWidth,
        style: theme,
      }),
      h(horizontalLineComp, {
        left: contentLeft,
        top: 6,
        length: contentWidth,
        lineCh: SingleChars.horizontal,
        style: theme,
      }),
      h(textLineComp, {
        align: TextAlign.center,
        left: contentLeft,
        top: 6,
        width: contentWidth,
        text: `Total: ${formatSize(props.total)}`,
        style: theme,
      }),
      h(progressBarComp, {
        percent: props.totalPercent,
        left: contentLeft,
        top: 7,
        length: contentWidth,
        style: theme,
      }),

      h(horizontalLineComp, {
        left: contentLeft,
        top: 8,
        length: contentWidth,
        lineCh: SingleChars.horizontal,
        style: theme,
      }),

      h("text", {
        left: contentLeft,
        top: 9,
        style: theme,
        content: `Time: ${_toTime(props.timeSeconds)} Left: ${_toTime(
          props.leftSeconds
        )}`,
      }),

      h(textLineComp, {
        align: TextAlign.right,
        left: contentLeft + 30,
        top: 9,
        width: contentWidth - 30,
        text: `${_toSpeed(props.bytesPerSecond * 8)}/s`,
        style: theme,
        padding: 0,
      }),

      h("button", { width: 0, height: 0 })
    )
  );
}
