/**
 * @import { Database } from "@farjs/better-sqlite3-wrapper"
 * @import { HistoryProvider } from "@farjs/filelist/history/HistoryProvider.mjs"
 * @import { FSServices } from "../../fs/FSServices.mjs"
 */
import HistoryKindDao from "../../dao/HistoryKindDao.mjs";
import FolderShortcutDao from "../../dao/FolderShortcutDao.mjs";
import FolderShortcutsService from "../../fs/popups/FolderShortcutsService.mjs";
import HistoryProviderImpl from "../service/HistoryProviderImpl.mjs";

class FileListModule {
  /**
   * @param {Database} db
   */
  constructor(db) {
    const historyKindDao = HistoryKindDao(db);
    const folderShortcutDao = FolderShortcutDao(db);
    const folderShortcuts = FolderShortcutsService(folderShortcutDao);

    /** @readonly @type {HistoryProvider} */
    this.historyProvider = HistoryProviderImpl(db, historyKindDao);

    /** @readonly @type {FSServices} */
    this.fsServices = {
      folderShortcuts,
    };
  }
}

export default FileListModule;
