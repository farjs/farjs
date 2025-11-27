/**
 * @import { ButtonsPanelAction } from "@farjs/ui/ButtonsPanel.mjs"
 */
import React, { useState } from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextBox from "@farjs/ui/TextBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";

const h = React.createElement;

/**
 * @typedef {"Add" | "Copy" | "Move"} AddToArchAction
 */

/**
 * @typedef {{
 *  readonly zipName: string;
 *  readonly action: AddToArchAction;
 *  onAction(zipName: string): void;
 *  onCancel(): void;
 * }} AddToArchPopupProps
 */

/**
 * @param {AddToArchPopupProps} props
 */
const AddToArchPopup = (props) => {
  const {
    modalComp,
    textLineComp,
    textBoxComp,
    horizontalLineComp,
    buttonsPanelComp,
  } = AddToArchPopup;

  const [zipName, setZipName] = useState(props.zipName);

  const [width, height] = [75, 8];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = Theme.useTheme().popup.regular;

  const onAction = () => {
    if (zipName.length > 0) {
      props.onAction(zipName);
    }
  };

  /** @type {readonly ButtonsPanelAction[]} */
  const actions = [
    { label: `[ ${props.action} ]`, onAction: onAction },
    { label: "[ Cancel ]", onAction: props.onCancel },
  ];

  return h(
    modalComp,
    {
      title: `${props.action} files to archive`,
      width,
      height,
      style: theme,
      onCancel: props.onCancel,
    },
    h(textLineComp, {
      align: TextAlign.left,
      left: contentLeft,
      top: 1,
      width: contentWidth,
      text: `${props.action} to zip archive:`,
      style: theme,
      padding: 0,
    }),
    h(textBoxComp, {
      left: contentLeft,
      top: 2,
      width: contentWidth,
      value: zipName,
      onChange: setZipName,
      onEnter: onAction,
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
      actions,
      style: theme,
      margin: 2,
    })
  );
};

AddToArchPopup.displayName = "AddToArchPopup";
AddToArchPopup.modalComp = Modal;
AddToArchPopup.textLineComp = TextLine;
AddToArchPopup.textBoxComp = TextBox;
AddToArchPopup.horizontalLineComp = HorizontalLine;
AddToArchPopup.buttonsPanelComp = ButtonsPanel;

export default AddToArchPopup;
