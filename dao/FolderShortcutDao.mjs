import Database from "@farjs/better-sqlite3-wrapper";

/**
 * @typedef {{
 *  readonly id: number;
 *  readonly path: string;
 * }} FolderShortcut
 */

/**
 * @typedef {{
 *  getAll(): Promise<readonly FolderShortcut[]>;
 *  save(entity: FolderShortcut): Promise<void>;
 *  delete(id: number): Promise<void>;
 *  deleteAll(): Promise<void>;
 * }} FolderShortcutDao
 */

const tableName = "folder_shortcuts";

/**
 *
 * @param {Database.Database} db
 * @returns {FolderShortcutDao}
 */
function FolderShortcutDao(db) {
  return {
    getAll: async () =>
      db.transaction(() => {
        const query = db.prepare(
          /* sql */ `SELECT id, path FROM ${tableName} ORDER BY id;`
        );
        return query.all();
      })(),

    save: async (entity) =>
      db.transaction(() => {
        db.prepare(
          /* sql */ `INSERT INTO ${tableName} (id, path) VALUES (?, ?)
            ON CONFLICT (id) DO UPDATE SET path = excluded.path;`
        ).run(entity.id, entity.path);
      })(),

    delete: async (id) =>
      db.transaction(() => {
        db.prepare(/* sql */ `DELETE FROM ${tableName} WHERE id = ?;`).run(id);
      })(),

    deleteAll: async () =>
      db.transaction(() => {
        db.prepare(/* sql */ `DELETE FROM ${tableName};`).run();
      })(),
  };
}

export default FolderShortcutDao;
