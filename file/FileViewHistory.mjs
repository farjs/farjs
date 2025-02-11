/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryKind} HistoryKind
 */
import { stripPrefix } from "@farjs/filelist/utils.mjs";

/**
 * @typedef {{
 *  readonly isEdit: boolean;
 *  readonly encoding: string;
 *  readonly position: number;
 *  readonly wrap?: boolean;
 *  readonly column?: number;
 * }} FileViewHistoryParams
 */

/**
 * @typedef {{
 *  readonly path: string;
 *  readonly params: FileViewHistoryParams;
 * }} FileViewHistory
 */

/**
 * @param {string} path
 * @param {FileViewHistoryParams} params
 * @returns {FileViewHistory}
 */
function FileViewHistory(path, params) {
  return { path, params };
}

/** @type {HistoryKind} */
FileViewHistory.fileViewsHistoryKind = {
  name: "farjs.fileViews",
  maxItemsCount: 150,
};

/**
 * @param {FileViewHistory} h
 * @returns {History}
 */
FileViewHistory.toHistory = (h) => {
  return {
    item: FileViewHistory.pathToItem(h.path, h.params.isEdit),
    params: h.params,
  };
};

/**
 * @param {History} h
 * @returns {FileViewHistory | undefined}
 */
FileViewHistory.fromHistory = (h) => {
  if (h.params) {
    return {
      path: FileViewHistory._itemToPath(h.item),
      params: /** @type {FileViewHistoryParams} */ (h.params),
    };
  }
  return undefined;
};

/**
 * @param {string} path
 * @param {boolean} isEdit
 * @returns {string}
 */
FileViewHistory.pathToItem = (path, isEdit) => {
  return isEdit ? `E:${path}` : `V:${path}`;
};

/**
 * @param {string} item
 * @returns {string}
 */
FileViewHistory._itemToPath = (item) => {
  if (item.startsWith("V:")) return stripPrefix(item, "V:");
  else if (item.startsWith("E:")) return stripPrefix(item, "E:");
  else return item;
};

export default FileViewHistory;
