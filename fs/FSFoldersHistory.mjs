/**
 * @import { HistoryKind } from "@farjs/filelist/history/HistoryProvider.mjs"
 */
import { useLayoutEffect } from "react";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";

/**
 * @typedef {{
 *  readonly currDirPath: string;
 * }} FSFoldersHistoryProps
 */

/**
 * @param {FSFoldersHistoryProps} props
 */
const FSFoldersHistory = (props) => {
  const { foldersHistoryKind } = FSFoldersHistory;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const currDirPath = props.currDirPath;

  const saveHistory = async () => {
    const foldersHistory = await historyProvider.get(foldersHistoryKind);
    await foldersHistory.save({ item: currDirPath });
  };

  useLayoutEffect(() => {
    if (currDirPath.length > 0) {
      saveHistory();
    }
  }, [currDirPath]);

  return null;
};

FSFoldersHistory.displayName = "FSFoldersHistory";

/** @type {HistoryKind} */
FSFoldersHistory.foldersHistoryKind = {
  name: "farjs.folders",
  maxItemsCount: 100,
};

export default FSFoldersHistory;
