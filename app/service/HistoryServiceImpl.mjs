/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryService} HistoryService
 * @typedef {import("../../dao/HistoryDao.mjs").HistoryDao} HistoryDao
 */

/**
 * @param {HistoryDao} dao
 * @returns {HistoryService}
 */
function HistoryServiceImpl(dao) {
  return {
    getAll: () => {
      return dao.getAll().catch((error) => {
        console.error(`Failed to read all history items, error: ${error}`);
        return [];
      });
    },

    getOne: (item) => {
      return dao.getByItem(item).catch((error) => {
        console.error(`Failed to read history item, error: ${error}`);
        return undefined;
      });
    },

    save: (h) => {
      return dao.save(h, Date.now()).catch((error) => {
        console.error(`Failed to save history item, error: ${error}`);
      });
    },
  };
}

export default HistoryServiceImpl;
