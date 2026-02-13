/**
 * @import StreamReader from "@farjs/filelist/util/StreamReader.mjs"
 * @typedef {import("@farjs/filelist/api/FileListDir.mjs").FileListDir} FileListDir
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @typedef {import("@farjs/filelist/util/SubProcess.mjs").SubProcess} SubProcess
 */
import FileListApi from "@farjs/filelist/api/FileListApi.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import { stripPrefix } from "@farjs/filelist/utils.mjs";
import SubProcess from "@farjs/filelist/util/SubProcess.mjs";
import ZipEntry from "./ZipEntry.mjs";

class ZipApi extends FileListApi {
  /**
   * @param {string} zipPath
   * @param {string} rootPath
   * @param {Promise<Map<string, readonly FileListItem[]>>} entriesByParentP
   */
  constructor(zipPath, rootPath, entriesByParentP) {
    super(false, new Set([FileListCapability.read, FileListCapability.delete]));

    /** @private @readonly @type {string} */
    this.zipPath = zipPath;

    /** @private @readonly @type {string} */
    this.rootPath = rootPath;

    /** @private @type {Promise<Map<string, readonly FileListItem[]>>} */
    this.entriesByParentP = entriesByParentP;
  }

  /**
   * @param {string} parent
   * @param {string} [dir]
   * @returns {Promise<FileListDir>}
   */
  async readDir(parent, dir) {
    const path = parent === "" ? this.rootPath : parent;
    const targetDir =
      dir === undefined
        ? path
        : (() => {
            if (dir === FileListItem.up.name) {
              const lastSlash = path.lastIndexOf("/");
              return path.substring(0, Math.max(lastSlash, 0));
            }

            return dir === FileListItem.currDir.name ? path : `${path}/${dir}`;
          })();

    const entriesByParent = await this.entriesByParentP;
    const parentPath = stripPrefix(stripPrefix(targetDir, this.rootPath), "/");
    const entries = entriesByParent.get(parentPath) ?? [];
    return FileListDir(targetDir, false, entries);
  }

  /**
   * @param {string} zipPath
   * @param {string} filePath
   * @returns {Promise<SubProcess>}
   */
  extract(zipPath, filePath) {
    return SubProcess.wrap(
      SubProcess.spawn("unzip", ["-p", zipPath, filePath], {
        windowsHide: true,
      }),
    );
  }

  /**
   * @param {string} zipFile
   * @param {string} parent
   * @param {Set<string>} items
   * @param {() => void} onNextItem
   * @returns {Promise<void>}
   */
  static async addToZip(zipFile, parent, items, onNextItem) {
    const subProcess = await SubProcess.wrap(
      SubProcess.spawn("zip", ["-r", zipFile, ...items], {
        cwd: parent,
        windowsHide: true,
      }),
    );
    await subProcess.stdout.readAllLines((line) => {
      if (line.includes("adding: ")) {
        onNextItem();
      }
    });
    const error = await subProcess.exitP;
    if (error && error.exitCode !== 0) {
      throw error;
    }
  }

  /**
   * @param {string} zipPath
   * @returns {Promise<Map<string, readonly FileListItem[]>>}
   */
  static async readZip(zipPath) {
    /** @type {(reader: StreamReader, result: Buffer[]) => Promise<readonly Buffer[]>} */
    async function loop(reader, result) {
      const content = await reader.readNextBytes(64 * 1024);
      if (content) {
        result.push(content);
        return loop(reader, result);
      }

      return result;
    }

    const subProcess = await SubProcess.wrap(
      SubProcess.spawn("unzip", ["-ZT", zipPath], {
        windowsHide: true,
      }),
    );
    const chunks = await loop(subProcess.stdout, []);
    const output = Buffer.concat(chunks).toString();
    const error = await subProcess.exitP;
    if (error && (error.exitCode !== 1 || !output.includes("Empty zipfile."))) {
      throw error;
    }

    return ZipEntry.groupByParent(ZipEntry.fromUnzipCommand(output));
  }
}

export default ZipApi;
