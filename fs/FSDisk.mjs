import { lazyFn } from "@farjs/filelist/utils.mjs";

/**
 * @typedef {{
 *  readonly root: string;
 *  readonly size: number;
 *  readonly free: number;
 *  readonly name: string;
 * }} FSDisk
 */

const FSDisk = {
  /** @type {(output: string) => readonly FSDisk[]} */
  fromDfCommand: (output) => {
    return parseOutput(dfRegex(), output).map((data) => {
      return {
        root: data.get("Mounted on") ?? "",
        size: toNumber(data.get("1024-blocks") ?? "") * 1024,
        free: toNumber(data.get("Available") ?? "") * 1024,
        name: data.get("Mounted on") ?? "",
      };
    });
  },

  /** @type {(output: string) => readonly FSDisk[]} */
  fromWmicLogicalDisk: (output) => {
    return parseOutput(wmicLogicalDiskRegex(), output).map((data) => {
      return {
        root: data.get("Caption") ?? "",
        size: toNumber(data.get("Size") ?? ""),
        free: toNumber(data.get("FreeSpace") ?? ""),
        name: data.get("VolumeName") ?? "",
      };
    });
  },
};

/**
 * @param {RegExp} regexIn
 * @param {string} output
 * @returns {Map<string, string>[]}
 */
function parseOutput(regexIn, output) {
  const lines = output.trim().split("\n");
  const headLine = lines[0];
  const regex = new RegExp(regexIn, "dg");
  /** @type {{column: string, start: number, end: number}[]}  */
  const columns = [];
  /** @type {RegExpExecArray | null}  */
  let regexRes = null;
  while ((regexRes = regex.exec(headLine)) !== null) {
    const indices = regexRes.indices;
    if (indices !== undefined) {
      const start = indices[1][0];
      const end = indices[1][1];
      const column = headLine.substring(start, end).trim();
      columns.push({ column, start, end });
    }
  }

  const lastColumnIdx = columns.length - 1;
  const res = lines.slice(1).map((line) => {
    return new Map(
      columns.map(({ column, start, end }, i) => {
        const until = i === lastColumnIdx ? line.length : end;

        return [column, line.slice(start, until).trim()];
      })
    );
  });
  return res;
}

/**
 * @param {string} s
 * @returns {number}
 */
function toNumber(s) {
  return s.length === 0 ? 0 : parseFloat(s);
}

/** @type {() => RegExp} */
const dfRegex = lazyFn(
  () =>
    /(Filesystem\s+|1024-blocks|\s+Used|\s+Available|\s+Capacity|\s+Mounted on\s*)/
);

/** @type {() => RegExp} */
const wmicLogicalDiskRegex = lazyFn(
  () => /(Caption\s*|FreeSpace\s*|Size\s*|VolumeName\s*)/
);

export default FSDisk;
