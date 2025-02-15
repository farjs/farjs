/**
 * @typedef {import("@farjs/ui/ButtonsPanel.mjs").ButtonsPanelAction} ButtonsPanelAction
 * @typedef {import("@farjs/ui/theme/Theme.mjs").ThemeEffects} ThemeEffects
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryKind} HistoryKind
 */
import React, { useLayoutEffect, useState } from "react";
import Modal from "@farjs/ui/popup/Modal.mjs";
import ModalContent from "@farjs/ui/popup/ModalContent.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import ComboBox from "@farjs/ui/ComboBox.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import CheckBox from "@farjs/ui/CheckBox.mjs";
import ButtonsPanel from "@farjs/ui/ButtonsPanel.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly multiple: boolean;
 *  onOk(folder: string, multiple: boolean): void;
 *  onCancel(): void;
 * }} MakeFolderPopupProps
 */

/**
 * @param {MakeFolderPopupProps} props
 */
const MakeFolderPopup = (props) => {
  const {
    modalComp,
    textLineComp,
    comboBoxComp,
    horizontalLineComp,
    checkBoxComp,
    buttonsPanelComp,
  } = MakeFolderPopup;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const [maybeItems, setItems] = useState(
    /** @type {readonly string[] | undefined} */ (undefined)
  );
  const [folderName, setFolderName] = useState("");
  const [multiple, setMultiple] = useState(props.multiple);
  const width = 75;
  const height = 10;
  const contentWidth = width - (ModalContent.paddingHorizontal + 2) * 2;
  const contentLeft = 2;
  /** @type {ThemeEffects} */
  const theme = Theme.useTheme().popup.regular;

  const onOk = () => {
    if (folderName.length > 0) {
      props.onOk(folderName, multiple);
    }
  };
  /** @type {readonly ButtonsPanelAction[]} */
  const actions = [
    { label: "[ OK ]", onAction: onOk },
    { label: "[ Cancel ]", onAction: props.onCancel },
  ];

  const readHistory = async () => {
    const mkDirsHistory = await historyProvider.get(
      MakeFolderPopup.mkDirsHistoryKind
    );
    const items = await mkDirsHistory.getAll();
    const itemsReversed = [...items].reverse().map((_) => _.item);
    if (itemsReversed.length > 0) {
      setFolderName(itemsReversed[0]);
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
        title: "Make Folder",
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
        text: "Create the folder",
        style: theme,
        padding: 0,
      }),
      h(comboBoxComp, {
        left: contentLeft,
        top: 2,
        width: contentWidth,
        items: maybeItems,
        value: folderName,
        onChange: (value) => {
          setFolderName(value);
        },
        onEnter: onOk,
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
      h(checkBoxComp, {
        left: contentLeft,
        top: 4,
        value: multiple,
        label: "Process multiple names",
        style: theme,
        onChange: () => {
          setMultiple(!multiple);
        },
      }),

      h(horizontalLineComp, {
        left: 0,
        top: 5,
        length: width - ModalContent.paddingHorizontal * 2,
        lineCh: SingleChars.horizontal,
        style: theme,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(buttonsPanelComp, {
        top: 6,
        actions: actions,
        style: theme,
        margin: 2,
      })
    );
  }

  return null;
};

MakeFolderPopup.displayName = "MakeFolderPopup";
MakeFolderPopup.modalComp = Modal;
MakeFolderPopup.textLineComp = TextLine;
MakeFolderPopup.comboBoxComp = ComboBox;
MakeFolderPopup.horizontalLineComp = HorizontalLine;
MakeFolderPopup.checkBoxComp = CheckBox;
MakeFolderPopup.buttonsPanelComp = ButtonsPanel;

/** @type {HistoryKind} */
MakeFolderPopup.mkDirsHistoryKind = { name: "farjs.mkdirs", maxItemsCount: 50 };

export default MakeFolderPopup;
