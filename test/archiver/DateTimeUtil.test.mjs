import { deepEqual } from "node:assert/strict";
import DateTimeUtil from "../../archiver/DateTimeUtil.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const parseDateTime = DateTimeUtil.parseDateTime;

describe("DateTimeUtil.test.mjs", () => {
  it("should parse date and time when parseDateTime", () => {
    //when & then
    deepEqual(parseDateTime(""), 0);
    deepEqual(parseDateTime("06-28-2019"), 0);
    deepEqual(parseDateTime("06-28-2019 16:0"), 0);
    deepEqual(
      parseDateTime("06-28-2019 16:09"),
      Date.parse("2019-06-28T16:09"),
    );
    deepEqual(
      parseDateTime("28.06.19 16:09"),
      Date.parse("2019-06-28T16:09:00"),
    );
    deepEqual(
      parseDateTime("28.06.99 16:09"),
      Date.parse("1999-06-28T16:09:00"),
    );
    deepEqual(
      parseDateTime("20190628.161923"),
      Date.parse("2019-06-28T16:19:23"),
    );
  });
});
