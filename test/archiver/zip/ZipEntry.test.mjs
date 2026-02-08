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

const { fromUnzipCommand, groupByParent } = ZipEntry;

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

  it("should infer dirs when groupByParent", () => {
    //given
    const zipEntries = [
      ZipEntry("dir 1/dir 2/dir 3", "file 3", false, 7, 8, "-rw-r--r--"),
      ZipEntry("dir 1/dir 2/dir 3", "file 4", false, 9, 10, "-rw-r--r--"),
      ZipEntry("dir 1/dir 2", "file 2", false, 5, 6, "-rw-r--r--"),
      ZipEntry("dir 1", "file 1", false, 2, 3, "-rw-r--r--"),
    ];

    //when & then
    deepEqual(
      groupByParent(zipEntries),
      new Map([
        [
          "dir 1/dir 2/dir 3",
          [
            ZipEntry("dir 1/dir 2/dir 3", "file 3", false, 7, 8, "-rw-r--r--"),
            ZipEntry("dir 1/dir 2/dir 3", "file 4", false, 9, 10, "-rw-r--r--"),
          ],
        ],
        [
          "dir 1/dir 2",
          [
            ZipEntry("dir 1/dir 2", "dir 3", true, 0, 8, "drw-r--r--"),
            ZipEntry("dir 1/dir 2", "file 2", false, 5, 6, "-rw-r--r--"),
          ],
        ],
        [
          "dir 1",
          [
            ZipEntry("dir 1", "dir 2", true, 0, 8, "drw-r--r--"),
            ZipEntry("dir 1", "file 1", false, 2, 3, "-rw-r--r--"),
          ],
        ],
        ["", [ZipEntry("", "dir 1", true, 0, 8, "drw-r--r--")]],
      ]),
    );
  });
});

/** @type {(s: string) => number} */
function dt(s) {
  return Date.parse(s);
}
