import assert from "node:assert/strict";
import FSDisk from "../../fs/FSDisk.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const { fromDfCommand, fromWmicLogicalDisk } = FSDisk;

describe("FSDisk.test.mjs", () => {
  it("should parse df output", () => {
    //when
    const results = fromDfCommand(
      `Filesystem   1024-blocks      Used Available Capacity  Mounted on
/dev/disk1s1   244912536 202577024  40612004    84%    /
/dev/disk1s4   244912536   1048632  40612004     3%    /private/var/vm
/dev/disk1s3   244912536    498596  40612004     2%    /Volumes/Recovery
`
    );

    //then
    assert.deepEqual(results, [
      { root: "/", size: 250790436864, free: 41586692096, name: "/" },
      {
        root: "/private/var/vm",
        size: 250790436864,
        free: 41586692096,
        name: "/private/var/vm",
      },
      {
        root: "/Volumes/Recovery",
        size: 250790436864,
        free: 41586692096,
        name: "/Volumes/Recovery",
      },
    ]);
  });

  it("should use default values if not found in df output", () => {
    //when & then
    assert.deepEqual(
      fromDfCommand(
        `Filesystem
/dev/disk1s1
`
      ),
      [{ root: "", size: 0, free: 0, name: "" }]
    );
  });

  it("should parse wmic logicaldisk output", () => {
    //when
    const results = fromWmicLogicalDisk(
      `Caption  FreeSpace     Size          VolumeName
C:       81697124352   156595318784  SYSTEM
D:       352966430720  842915639296  DATA
E:
`
    );

    //then
    assert.deepEqual(results, [
      { root: "C:", size: 156595318784, free: 81697124352, name: "SYSTEM" },
      { root: "D:", size: 842915639296, free: 352966430720, name: "DATA" },
      { root: "E:", size: 0, free: 0, name: "" },
    ]);
  });

  it("should use default values if not found in wmic logicaldisk output", () => {
    //when & then
    assert.deepEqual(
      fromWmicLogicalDisk(
        `Test
T:
`
      ),
      [{ root: "", size: 0, free: 0, name: "" }]
    );
  });
});
