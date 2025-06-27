/**
 * @typedef {import("./HistoryKindDao.mjs").HistoryKindEntity} HistoryKindEntity
 */
import Database from "@farjs/better-sqlite3-wrapper";

/**
 * @typedef {{
 *  readonly item: string;
 *  readonly params?: object;
 * }} History
 */

/**
 * @typedef {{
 *  getAll(): Promise<readonly History[]>;
 *  getByItem(item: string): Promise<History | undefined>;
 *  save(entity: History, updatedAt: number): Promise<void>;
 *  deleteAll(): Promise<void>;
 * }} HistoryDao
 */

const tableName = "history";

/**
 * @param {any} r
 * @returns {History}
 */
function rowExtractor(r) {
  return {
    item: r.item,
    params: r.params ? JSON.parse(r.params) : undefined,
  };
}

/**
 *
 * @param {Database.Database} db
 * @param {HistoryKindEntity} kind
 * @param {number} maxItemsCount
 * @returns {HistoryDao}
 */
function HistoryDao(db, kind, maxItemsCount) {
  return {
    getAll: async () =>
      db.transaction(() => {
        const query = db.prepare(
          /* sql */ `SELECT item, params FROM ${tableName} WHERE kind_id = ? ORDER BY updated_at;`
        );
        return query.all(kind.id).map(rowExtractor);
      })(),

    getByItem: async (item) =>
      db.transaction(() => {
        const query = db.prepare(
          /* sql */ `SELECT item, params FROM ${tableName} WHERE kind_id = ? AND item = ?;`
        );
        const row = query.get(kind.id, item);
        return row ? rowExtractor(row) : undefined;
      })(),

    save: async (entity, updatedAt) =>
      db.transaction(() => {
        db.prepare(
          /* sql */ `INSERT INTO ${tableName} (kind_id, item, params, updated_at) VALUES (?, ?, ?, ?)
            ON CONFLICT (kind_id, item) DO UPDATE SET
              params = excluded.params,
              updated_at = excluded.updated_at;`
        ).run(
          kind.id,
          entity.item,
          entity.params ? JSON.stringify(entity.params) : null,
          updatedAt
        );

        db.prepare(
          /* sql */ `DELETE FROM ${tableName} WHERE kind_id = ? AND updated_at <
            (SELECT min(updated_at) FROM (
              SELECT updated_at FROM ${tableName} WHERE kind_id = ? ORDER BY updated_at DESC LIMIT ?
            ));`
        ).run(kind.id, kind.id, maxItemsCount);
      })(),

    deleteAll: async () =>
      db.transaction(() => {
        db.prepare(/* sql */ `DELETE FROM ${tableName};`).run();
      })(),
  };
}

export default HistoryDao;
