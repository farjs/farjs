/**
 * @import { ButtonsPanelAction } from "@farjs/ui/ButtonsPanel.mjs"
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 */
import React from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";

const h = React.createElement;

/**
 * @typedef {"Overwrite"
 *  | "All"
 *  | "Skip"
 *  | "SkipAll"
 *  | "Append"
 * } FileExistsAction
 */

/**
 * @typedef {{
 *  readonly newItem: FileListItem;
 *  readonly existing: FileListItem;
 *  onAction(action: FileExistsAction): void;
 *  onCancel(): void;
 * }} FileExistsPopupProps
 */

/**
 * @param {FileExistsPopupProps} props
 */
const FileExistsPopup = (props) => {
  const { modalComp, textLineComp, horizontalLineComp, buttonsPanelComp } =
    FileExistsPopup;

  const [width, height] = [58, 11];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = Theme.useTheme().popup.error;

  /** @type {(label: string, onAction: () => void) => ButtonsPanelAction} */
  function buttonAction(label, onAction) {
    return { label, onAction };
  }

  /** @type {readonly ButtonsPanelAction[]} */
  const actions = [
    buttonAction("Overwrite", () => props.onAction("Overwrite")),
    buttonAction("All", () => props.onAction("All")),
    buttonAction("Skip", () => props.onAction("Skip")),
    buttonAction("Skip all", () => props.onAction("SkipAll")),
    buttonAction("Append", () => props.onAction("Append")),
    buttonAction("Cancel", props.onCancel),
  ];

  return h(
    modalComp,
    {
      title: "Warning",
      width,
      height,
      style: theme,
      onCancel: props.onCancel,
    },
    h("text", {
      left: "center",
      top: 1,
      style: theme,
      content: "File already exists",
    }),
    h(textLineComp, {
      align: TextAlign.center,
      left: contentLeft,
      top: 2,
      width: contentWidth,
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
      left: contentLeft,
      top: 4,
      style: theme,
      content: `New
Existing`,
    }),
    h(textLineComp, {
      align: TextAlign.right,
      left: contentLeft,
      top: 4,
      width: contentWidth,
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
      left: contentLeft,
      top: 5,
      width: contentWidth,
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
      length: width - ModalContent.paddingHorizontal * 2,
      lineCh: SingleChars.horizontal,
      style: theme,
      startCh: DoubleChars.leftSingle,
      endCh: DoubleChars.rightSingle,
    }),
    h(buttonsPanelComp, {
      top: 7,
      actions: actions,
      style: theme,
      padding: 1,
    })
  );
};

FileExistsPopup.displayName = "FileExistsPopup";
FileExistsPopup.modalComp = Modal;
FileExistsPopup.textLineComp = TextLine;
FileExistsPopup.horizontalLineComp = HorizontalLine;
FileExistsPopup.buttonsPanelComp = ButtonsPanel;

export default FileExistsPopup;
