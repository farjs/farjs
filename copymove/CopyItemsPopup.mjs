/**
 * @import { ButtonsPanelAction } from "@farjs/ui/ButtonsPanel.mjs"
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import { HistoryKind } from "@farjs/filelist/history/HistoryProvider.mjs"
 */
import React, { useLayoutEffect, useState } from "react";
import Theme from "@farjs/ui/theme/Theme.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import Modal from "@farjs/ui/popup/Modal.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly move: boolean;
 *  readonly path: string;
 *  readonly items: readonly FileListItem[];
 *  onAction(path: string): void;
 *  onCancel(): void;
 * }} CopyItemsPopupProps
 */

/**
 * @param {CopyItemsPopupProps} props
 */
const CopyItemsPopup = (props) => {
  const {
    modalComp,
    textLineComp,
    comboBoxComp,
    horizontalLineComp,
    buttonsPanelComp,
  } = CopyItemsPopup;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const [maybeItems, setItems] = useState(
    /** @type {readonly string[] | undefined} */ (undefined)
  );
  const [path, setPath] = useState(props.path);

  const [width, height] = [75, 8];
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  const theme = Theme.useTheme().popup.regular;

  const count = props.items.length;
  const maybeFirstItem = count > 0 ? props.items[0] : undefined;
  const itemsText =
    count > 1
      ? `${count} items`
      : maybeFirstItem !== undefined
      ? `"${maybeFirstItem.name}"`
      : "";

  const onCopy = () => {
    if (path.length > 0) {
      props.onAction(path);
    }
  };

  const title = props.move ? "Rename/Move" : "Copy";
  const text = props.move ? "Rename or move" : "Copy";
  const action = props.move ? "Rename" : "Copy";
  /** @type {readonly ButtonsPanelAction[]} */
  const actions = [
    { label: `[ ${action} ]`, onAction: onCopy },
    { label: "[ Cancel ]", onAction: props.onCancel },
  ];

  const readHistory = async () => {
    const copyItemsHistory = await historyProvider.get(
      CopyItemsPopup.copyItemsHistoryKind
    );
    const items = await copyItemsHistory.getAll();
    const itemsReversed = items.map((_) => _.item).reverse();
    setItems(itemsReversed);
  };

  useLayoutEffect(() => {
    readHistory();
  }, []);

  if (maybeItems !== undefined) {
    return h(
      modalComp,
      {
        title,
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
        text: `${text} ${itemsText} to:`,
        style: theme,
        padding: 0,
      }),
      h(comboBoxComp, {
        left: contentLeft,
        top: 2,
        width: contentWidth,
        items: maybeItems,
        value: path,
        onChange: setPath,
        onEnter: onCopy,
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
  }

  return null;
};

CopyItemsPopup.displayName = "CopyItemsPopup";
CopyItemsPopup.modalComp = Modal;
CopyItemsPopup.textLineComp = TextLine;
CopyItemsPopup.comboBoxComp = ComboBox;
CopyItemsPopup.horizontalLineComp = HorizontalLine;
CopyItemsPopup.buttonsPanelComp = ButtonsPanel;

/** @type {HistoryKind} */
CopyItemsPopup.copyItemsHistoryKind = {
  name: "farjs.copyItems",
  maxItemsCount: 50,
};

export default CopyItemsPopup;
