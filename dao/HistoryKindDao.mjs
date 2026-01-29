import Database from "@farjs/better-sqlite3-wrapper";

/**
 * @typedef {{
 *  readonly id: number;
 *  readonly name: string;
 * }} HistoryKindEntity
 */

/**
 * @typedef {{
 *  getAll(): Promise<readonly HistoryKindEntity[]>;
 *  upsert(entity: HistoryKindEntity): Promise<HistoryKindEntity>;
 *  deleteAll(): Promise<void>;
 * }} HistoryKindDao
 */

const tableName = "history_kinds";

/**
 *
 * @param {Database.Database} db
 * @returns {HistoryKindDao}
 */
function HistoryKindDao(db) {
  return {
    getAll: async () =>
      db.transaction(() => {
        const query = db.prepare(
          /* sql */ `SELECT id, name FROM ${tableName} ORDER BY id;`,
        );
        return /** @type {HistoryKindEntity[]} */ (query.all());
      })(),

    upsert: async (entity) =>
      db.transaction(() => {
        db.prepare(
          /* sql */ `INSERT INTO ${tableName} (name) VALUES (?) ON CONFLICT (name) DO NOTHING;`,
        ).run(entity.name);

        const query = db.prepare(
          /* sql */ `SELECT id, name FROM ${tableName} WHERE name = ?;`,
        );
        return /** @type {HistoryKindEntity} */ (query.get(entity.name));
      })(),

    deleteAll: async () =>
      db.transaction(() => {
        db.prepare(/* sql */ `DELETE FROM ${tableName};`).run();
      })(),
  };
}

export default HistoryKindDao;
