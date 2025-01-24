/**
 * @typedef {import("@farjs/ui/theme/Theme.mjs").ThemeEffects} ThemeEffects
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryKind} HistoryKind
 */
import React, { useLayoutEffect, useState } from "react";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showSelect: boolean;
 *  onAction(pattern: string): void;
 *  onCancel(): void;
 * }} SelectPopupProps
 */

/**
 * @param {SelectPopupProps} props
 */
const SelectPopup = (props) => {
  const { modalComp, comboBoxComp } = SelectPopup;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const [maybeItems, setItems] = useState(
    /** @type {string[] | undefined} */ (undefined)
  );
  const [pattern, setPattern] = useState("");
  const width = 55;
  const height = 5;
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  /** @type {ThemeEffects} */
  const theme = Theme.useTheme().popup.regular;

  const onAction = () => {
    if (pattern.length > 0) {
      props.onAction(pattern);
    }
  };

  const readHistory = async () => {
    const selectPatternsHistory = await historyProvider.get(
      SelectPopup.selectPatternsHistoryKind
    );
    const items = await selectPatternsHistory.getAll();
    const itemsReversed = [...items].reverse().map((_) => _.item);
    if (itemsReversed.length > 0) {
      setPattern(itemsReversed[0]);
    }
    setItems(itemsReversed);
  };

  useLayoutEffect(() => {
    readHistory();
  }, []);

  if (maybeItems !== undefined) {
    return h(
      modalComp,
      {
        title: props.showSelect ? "Select" : "Deselect",
        width,
        height,
        style: theme,
        onCancel: props.onCancel,
      },
      h(comboBoxComp, {
        left: contentLeft,
        top: 1,
        width: contentWidth,
        items: maybeItems,
        value: pattern,
        onChange: (value) => {
          setPattern(value);
        },
        onEnter: onAction,
      })
    );
  }

  return null;
};

SelectPopup.displayName = "SelectPopup";
SelectPopup.modalComp = Modal;
SelectPopup.comboBoxComp = ComboBox;

/** @type {HistoryKind} */
SelectPopup.selectPatternsHistoryKind = {
  name: "farjs.selectPatterns",
  maxItemsCount: 50,
};

export default SelectPopup;
