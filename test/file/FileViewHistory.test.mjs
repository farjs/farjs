/**
 * @typedef {import("@farjs/filelist/history/HistoryProvider.mjs").History} History
 * @typedef {import("../../file/FileViewHistory.mjs").FileViewHistoryParams} FileViewHistoryParams
 */
import assert from "node:assert/strict";
import FileViewHistory from "../../file/FileViewHistory.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FileViewHistory.test.mjs", () => {
  /** @type {FileViewHistoryParams} */
  const params = {
    isEdit: false,
    encoding: "utf8",
    position: 123,
    wrap: true,
    column: 2,
  };

  const { toHistory, fromHistory, pathToItem, _itemToPath } = FileViewHistory;

  it("should define fileViewsHistoryKind", () => {
    //when & then
    assert.deepEqual(FileViewHistory.fileViewsHistoryKind, {
      name: "farjs.fileViews",
      maxItemsCount: 150,
    });
  });

  it("should convert history when toHistory", () => {
    //given
    const h = FileViewHistory("/test/path", params);

    //when
    const result = toHistory(h);

    //then
    assert.deepEqual(result, { item: "V:/test/path", params: h.params });
  });

  it("should convert history when fromHistory", () => {
    //given
    /** @type {History} */
    const h = {
      item: "V:/test/path",
      params,
    };

    //when
    const result = fromHistory(h);

    //then
    assert.deepEqual(result, { path: "/test/path", params: h.params });

    //when & then
    assert.deepEqual(fromHistory({ ...h, params: undefined }), undefined);
  });

  it("should convert path to item when pathToItem", () => {
    //when & then
    assert.deepEqual(pathToItem("test/path", false), "V:test/path");
    assert.deepEqual(pathToItem("test/path", true), "E:test/path");
  });

  it("should convert item to path when _itemToPath", () => {
    //when & then
    assert.deepEqual(_itemToPath("V:test/path"), "test/path");
    assert.deepEqual(_itemToPath("E:test/path"), "test/path");
    assert.deepEqual(_itemToPath("D:test/path"), "D:test/path");
  });
});
