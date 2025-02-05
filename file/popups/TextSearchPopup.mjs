/**
 * @typedef {import("@farjs/ui/theme/Theme.mjs").ThemeEffects} ThemeEffects
 * @typedef {import("@farjs/ui/ButtonsPanel.mjs").ButtonsPanelAction} ButtonsPanelAction
 */
import React, { useState } from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  onSearch(searchText: string): void;
 *  onCancel(): void;
 * }} TextSearchPopupProps
 */

/**
 * @param {TextSearchPopupProps} props
 */
const TextSearchPopup = (props) => {
  const {
    modalComp,
    textLineComp,
    comboBoxComp,
    horizontalLineComp,
    buttonsPanelComp,
  } = TextSearchPopup;

  const [searchText, setSearchText] = useState("");
  const width = 75;
  const height = 8;
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  /** @type {ThemeEffects} */
  const theme = Theme.useTheme().popup.regular;

  const onSearch = () => {
    if (searchText.length > 0) {
      props.onSearch(searchText);
    }
  };

  /** @type {ButtonsPanelAction[]} */
  const actions = [
    { label: "[ Search ]", onAction: onSearch },
    { label: "[ Cancel ]", onAction: props.onCancel },
  ];

  return h(
    modalComp,
    {
      title: "Search",
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
      text: "Search for",
      style: theme,
      padding: 0,
    }),
    h(comboBoxComp, {
      left: contentLeft,
      top: 2,
      width: contentWidth,
      items: [],
      value: searchText,
      onChange: (value) => {
        setSearchText(value);
      },
      onEnter: onSearch,
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
      actions: actions,
      style: theme,
      margin: 2,
    })
  );
};

TextSearchPopup.displayName = "TextSearchPopup";
TextSearchPopup.modalComp = Modal;
TextSearchPopup.textLineComp = TextLine;
TextSearchPopup.comboBoxComp = ComboBox;
TextSearchPopup.horizontalLineComp = HorizontalLine;
TextSearchPopup.buttonsPanelComp = ButtonsPanel;

export default TextSearchPopup;
