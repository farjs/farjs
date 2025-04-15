/**
 * @import { FolderShortcutsService } from "./FolderShortcutsService.mjs"
 */

/**
 * @typedef {{
 *  getAll?: FolderShortcutsService['getAll'];
 *  save?: FolderShortcutsService['save'];
 *  delete?: FolderShortcutsService['delete'];
 * }} FolderShortcutsServiceMocks
 */

/**
 * @implements {FolderShortcutsService}
 */
class MockFolderShortcutsService {
  /**
   * @param {FolderShortcutsServiceMocks} mocks
   */
  constructor({ getAll, save, delete: del } = {}) {
    this.getAll = getAll ?? this.getAll;
    this.save = save ?? this.save;
    this.delete = del ?? this.delete;
  }

  /** @type {FolderShortcutsService['getAll']} */
  getAll() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FolderShortcutsService['save']} */
  save() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FolderShortcutsService['delete']} */
  delete() {
    return Promise.reject(new Error("Not implemented!"));
  }
}

export default MockFolderShortcutsService;
