/**
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import StreamReader from "@farjs/filelist/util/StreamReader.mjs"
 */
import child_process from "child_process";
import SubProcess from "@farjs/filelist/util/SubProcess.mjs";
import ZipEntry from "./ZipEntry.mjs";

class ZipApi {
  /**
   * @param {string} zipFile
   * @param {string} parent
   * @param {Set<string>} items
   * @param {() => void} onNextItem
   * @returns {Promise<void>}
   */
  static async addToZip(zipFile, parent, items, onNextItem) {
    const subProcess = await ZipApi.wrap(
      ZipApi.spawn("zip", ["-r", zipFile, ...items], {
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

    const subProcess = await ZipApi.wrap(
      ZipApi.spawn("unzip", ["-ZT", zipPath], {
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

  static spawn = child_process.spawn;
  static wrap = SubProcess.wrap;
}

export default ZipApi;
