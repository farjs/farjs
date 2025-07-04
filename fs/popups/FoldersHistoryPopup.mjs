import React, { useLayoutEffect, useState } from "react";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import FSFoldersHistory from "../FSFoldersHistory.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  onChangeDir(dir: string): void;
 *  onClose(): void;
 * }} FoldersHistoryPopupProps
 */

/**
 * @param {FoldersHistoryPopupProps} props
 */
const FoldersHistoryPopup = (props) => {
  const { listPopup } = FoldersHistoryPopup;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const [maybeItems, setItems] = useState(
    /** @type {readonly string[] | undefined} */ (undefined)
  );

  const readHistory = async () => {
    const foldersHistory = await historyProvider.get(
      FSFoldersHistory.foldersHistoryKind
    );
    const items = await foldersHistory.getAll();
    setItems(items.map((_) => _.item));
  };

  useLayoutEffect(() => {
    readHistory();
  }, []);

  if (maybeItems !== undefined) {
    return h(listPopup, {
      title: "Folders history",
      items: maybeItems,
      onAction: (index) => {
        props.onChangeDir(maybeItems[index]);
      },
      onClose: props.onClose,
      selected: Math.max(maybeItems.length - 1, 0),
    });
  }

  return null;
};

FoldersHistoryPopup.displayName = "FoldersHistoryPopup";
FoldersHistoryPopup.listPopup = ListPopup;

export default FoldersHistoryPopup;
