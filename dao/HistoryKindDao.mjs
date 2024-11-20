/**
 * @typedef {{
 *  readonly id: number;
 *  readonly name: string;
 * }} HistoryKindEntity
 */

const HistoryKindDao = {
  /** @type {() => Promise<HistoryKindEntity[]>} */
  getAll: () => {
    return Promise.resolve([]);
  },
};

export default HistoryKindDao;
