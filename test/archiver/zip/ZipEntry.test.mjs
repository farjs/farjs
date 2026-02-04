import { deepEqual } from "node:assert/strict";
import ZipEntry from "../../../archiver/zip/ZipEntry.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fromUnzipCommand = ZipEntry.fromUnzipCommand;

describe("ZipEntry.test.mjs", () => {
  it("should create ZipEntry with default params", () => {
    //when & then
    deepEqual(ZipEntry("test/parent", "test.name"), {
      parent: "test/parent",
      name: "test.name",
      isDir: false,
      isSymLink: false,
      size: 0,
      atimeMs: 0,
      mtimeMs: 0,
      ctimeMs: 0,
      birthtimeMs: 0,
      permissions: "",
    });
  });

  it("should handle empty zip-archive", () => {
    //when & then
    deepEqual(
      fromUnzipCommand(`Archive:  /test/dir/file.zip
Zip file size: 0 bytes, number of entries: 0
0 files
`),
      [],
    );
  });

  it("should parse unzip -ZT output", () => {
    //when & then
    deepEqual(
      fromUnzipCommand(`Archive:  /test/dir/file.zip
Zip file size: 595630 bytes, number of entries: 18
drwxr-xr-x  2.1 unx         0 bx stor 20190628.160903 test/
drwxr-xr-x  2.1 unx         0 bx stor 20190628.161923 test/dir/
-rw-r--r--  2.1 unx 123456789 bX defN 20190628.161924 test/dir/file.txt
18 files, 694287 bytes uncompressed, 591996 bytes compressed:  14.7%
`),
      [
        ZipEntry("", "test", true, 0, dt("2019-06-28T16:09:03"), "drwxr-xr-x"),
        ZipEntry(
          "test",
          "dir",
          true,
          0,
          dt("2019-06-28T16:19:23"),
          "drwxr-xr-x",
        ),
        ZipEntry(
          "test/dir",
          "file.txt",
          false,
          123456789,
          dt("2019-06-28T16:19:24"),
          "-rw-r--r--",
        ),
      ],
    );
  });

  it("should skip unparsable lines", () => {
    //when & then
    deepEqual(
      fromUnzipCommand(`Archive:  /test/dir/file.zip
Zip file size: 595630 bytes, number of entries: 18
drwxr-xr-x  2.1 unx         0 bx stor 20190628.160903 test/

r--r--  2.1 unx 12345 bX 01902811924 test/dir/file.txt

18 files, 694287 bytes uncompressed, 591996 bytes compressed:  14.7%
`),
      [ZipEntry("", "test", true, 0, dt("2019-06-28T16:09:03"), "drwxr-xr-x")],
    );
  });
});

/** @type {(s: string) => number} */
function dt(s) {
  return Date.parse(s);
}
