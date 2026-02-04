/**
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 */
import { lazyFn, stripPrefix, stripSuffix } from "@farjs/filelist/utils.mjs";
import DateTimeUtil from "../DateTimeUtil.mjs";

/**
 * @typedef {FileListItem & {
 *  readonly parent: string;
 * }} ZipEntry
 */

/**
 * @param {string} parent
 * @param {string} name
 * @param {boolean} [isDir]
 * @param {number} [size]
 * @param {number} [datetimeMs]
 * @param {string} [permissions]
 * @returns {ZipEntry}
 */
function ZipEntry(parent, name, isDir, size, datetimeMs, permissions) {
  const entryTimeMs = datetimeMs ?? 0;
  return {
    parent,
    name,
    isDir: isDir ?? false,
    isSymLink: false,
    size: size ?? 0,
    atimeMs: entryTimeMs,
    mtimeMs: entryTimeMs,
    ctimeMs: entryTimeMs,
    birthtimeMs: entryTimeMs,
    permissions: permissions ?? "",
  };
}

/**
 * @param {string} output
 * @returns {ZipEntry[]}
 */
ZipEntry.fromUnzipCommand = (output) => {
  return output
    .trim()
    .split("\n")
    .map((line) => {
      const regexRes = itemRegex().exec(line);
      if (regexRes) {
        const permissions = regexRes[1];
        const size = parseFloat(regexRes[4]);
        const datetime = regexRes[7];
        const pathName = regexRes[8];

        const path = stripSuffix(pathName, "/");
        const lastSlash = path.lastIndexOf("/");
        const [parent, name] =
          lastSlash !== -1
            ? [path.substring(0, lastSlash), path.substring(lastSlash + 1)]
            : ["", path];

        return ZipEntry(
          parent,
          stripPrefix(name, "/"),
          pathName.endsWith("/"),
          size,
          DateTimeUtil.parseDateTime(datetime),
          permissions,
        );
      }
      return undefined;
    })
    .filter((_) => _ !== undefined);
};

/** @type {() => RegExp} */
const itemRegex = lazyFn(
  () => /([d|-].+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+?)\s+(.+)/,
);

export default ZipEntry;
