import React from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import ProgressBar from "@farjs/ui/ProgressBar.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly move: boolean;
 *  readonly item: string;
 *  readonly to: string;
 *  readonly itemPercent: number;
 *  readonly total: number;
 *  readonly totalPercent: number;
 *  readonly timeSeconds: number;
 *  readonly leftSeconds: number;
 *  readonly bytesPerSecond: number;
 *  onCancel(): void;
 * }} CopyProgressPopupProps
 */

/**
 * @param {CopyProgressPopupProps} props
 */
const CopyProgressPopup = (props) => {
  const {
    modalComp,
    textLineComp,
    horizontalLineComp,
    progressBarComp,
    _toTime,
    _toSpeed,
  } = CopyProgressPopup;

  const [width, height] = [50, 13];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = Theme.useTheme().popup.regular;

  return h(
    modalComp,
    {
      title: props.move ? "Move" : "Copy",
      width,
      height,
      style: theme,
      onCancel: props.onCancel,
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
        props.leftSeconds,
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

    //for capturing inputs
    h("button", { width: 0, height: 0 }),
  );
};

CopyProgressPopup.displayName = "CopyProgressPopup";
CopyProgressPopup.modalComp = Modal;
CopyProgressPopup.textLineComp = TextLine;
CopyProgressPopup.horizontalLineComp = HorizontalLine;
CopyProgressPopup.progressBarComp = ProgressBar;

/** @type {(seconds: number) => string} */
CopyProgressPopup._toTime = (seconds) => {
  const hrs = Math.round(seconds / 3600);
  const min = Math.round((seconds - hrs * 3600) / 60);
  const sec = Math.round(seconds - hrs * 3600 - min * 60);

  return `${pad(hrs)}:${pad(min)}:${pad(sec)}`;
};

/** @type {(n: number) => string} */
function pad(n) {
  return formatSize(n).padStart(2, "0");
}

/** @type {(bits: number) => string} */
CopyProgressPopup._toSpeed = (bits) => {
  const [speed, mod] = (() => {
    return bits >= 100000000000
      ? [bits / 1000000000, "Gb"]
      : bits >= 100000000
        ? [bits / 1000000, "Mb"]
        : bits >= 100000
          ? [bits / 1000, "Kb"]
          : [bits, "b"];
  })();

  return `${formatSize(speed)}${mod}`;
};

export default CopyProgressPopup;
