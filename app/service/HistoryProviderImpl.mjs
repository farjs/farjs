/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryService} HistoryService
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").HistoryProvider} HistoryProvider
 * @typedef {import("../../dao/HistoryKindDao.mjs").HistoryKindDao} HistoryKindDao
 */
import Database from "@farjs/better-sqlite3-wrapper";
import HistoryDao from "../../dao/HistoryDao.mjs";
import HistoryServiceImpl from "./HistoryServiceImpl.mjs";

/**
 * @param {Database.Database} db
 * @param {HistoryKindDao} kindDao
 * @returns {HistoryProvider}
 */
function HistoryProviderImpl(db, kindDao) {
  /** @type {Record<string, HistoryService>} */
  const services = {};

  /**@type {HistoryService} */
  const noopService = {
    getAll: () => Promise.resolve([]),
    getOne: () => Promise.resolve(undefined),
    save: () => Promise.resolve(undefined),
  };

  return {
    get: (kind) => {
      const service = services[kind.name];
      if (service) {
        return Promise.resolve(service);
      }

      return kindDao
        .upsert({ id: -1, name: kind.name })
        .then((kindEntity) => {
          const service = HistoryServiceImpl(
            HistoryDao(
              db,
              kindEntity,
              HistoryProviderImpl._limitMaxItemsCount(kind.maxItemsCount)
            )
          );
          services[kind.name] = service;
          return service;
        })
        .catch((error) => {
          console.error(
            `Failed to upsert history kind '${kind.name}', error: ${error}`
          );
          return noopService;
        });
    },
  };
}

/**
 * @param {number} maxCount
 * @returns {number}
 */
HistoryProviderImpl._limitMaxItemsCount = (maxCount) => {
  return Math.min(Math.max(maxCount, 5), 150);
};

export default HistoryProviderImpl;
