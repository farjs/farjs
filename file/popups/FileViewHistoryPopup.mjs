/**
 * @typedef {import("../FileViewHistory.mjs").FileViewHistory} FileViewHistory
 */
import React, { useLayoutEffect, useState } from "react";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import FileViewHistory from "../FileViewHistory.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  onAction(h: FileViewHistory): void;
 *  onClose(): void;
 * }} FileViewHistoryPopupProps
 */

/**
 * @param {FileViewHistoryPopupProps} props
 */
const FileViewHistoryPopup = (props) => {
  const { listPopup } = FileViewHistoryPopup;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const [maybeItems, setItems] = useState(
    /** @type {readonly FileViewHistory[] | undefined} */ (undefined)
  );

  const readHistory = async () => {
    const fileViewsHistory = await historyProvider.get(
      FileViewHistory.fileViewsHistoryKind
    );
    const items = await fileViewsHistory.getAll();
    const historyItems = items.reduce((res, item) => {
      const h = FileViewHistory.fromHistory(item);
      if (h) {
        res.push(h);
      }
      return res;
    }, /** @type {FileViewHistory[]} */ ([]));
    setItems(historyItems);
  };

  useLayoutEffect(() => {
    readHistory();
  }, []);

  if (maybeItems !== undefined) {
    return h(listPopup, {
      title: "File view history",
      items: maybeItems.map((item) => {
        const prefix = item.params.isEdit ? "Edit: " : "View: ";
        return `${prefix}${item.path}`;
      }),
      onAction: (index) => {
        props.onAction(maybeItems[index]);
      },
      onClose: props.onClose,
      selected: Math.max(maybeItems.length - 1, 0),
      itemWrapPrefixLen: 9,
    });
  }

  return null;
};

FileViewHistoryPopup.displayName = "FileViewHistoryPopup";
FileViewHistoryPopup.listPopup = ListPopup;

export default FileViewHistoryPopup;
