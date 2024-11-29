/**
 * @typedef {import("../../dao/FolderShortcutDao.mjs").FolderShortcutDao} FolderShortcutDao
 */

/**
 * @typedef {{
 *  getAll(): Promise<(string | undefined)[]>;
 *  save(index: number, path: string): Promise<void>;
 *  delete(index: number): Promise<void>;
 * }} FolderShortcutsService
 */

/**
 *
 * @param {FolderShortcutDao} dao
 * @returns {FolderShortcutsService}
 */
function FolderShortcutsService(dao) {
  return {
    getAll: () => {
      return dao
        .getAll()
        .then((shortcuts) => {
          const res = new Array(10).fill(undefined);
          shortcuts.slice(0, Math.max(10, 0)).forEach((shortcut) => {
            res[shortcut.id] = shortcut.path;
          });
          return res;
        })
        .catch((error) => {
          console.error(`Failed to read folder shortcuts, error: ${error}`);
          return [];
        });
    },

    save: (index, path) => {
      return dao.save({ id: index, path }).catch((error) => {
        console.error(`Failed to save folder shortcut, error: ${error}`);
      });
    },

    delete: (index) => {
      return dao.delete(index).catch((error) => {
        console.error(`Failed to delete folder shortcut, error: ${error}`);
      });
    },
  };
}

export default FolderShortcutsService;
