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

  /** @type {FileListApi['readDir']} */
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

  /** @type {FileListApi['readFile']} */
  async readFile(parent, item) {
    const filePath = stripPrefix(
      stripPrefix(`${parent}/${item.name}`, this.rootPath),
      "/",
    );
    const { stdout, exitP } = await this.extract(this.zipPath, filePath);

    let pos = 0;
    return {
      file: filePath,

      readNextBytes: async (buff) => {
        const content = await stdout.readNextBytes(buff.length);
        if (content !== undefined) {
          const bytesRead = content.length;
          content.copy(buff, 0, 0, bytesRead);
          pos += bytesRead;
          return bytesRead;
        }

        if (pos !== item.size) {
          const error = await exitP;
          if (error) {
            throw error;
          }
        }

        return 0;
      },

      close: async () => {
        if (pos !== item.size) {
          stdout.readable.destroy();
        }

        await exitP;
      },
    };
  }

  /** @type {FileListApi['delete']} */
  delete(parent, items) {
    const self = this;

    /** @type {(parent: string, items: readonly FileListItem[]) => void} */
    function deleteFromState(parent, items) {
      self.entriesByParentP = self.entriesByParentP.then((entriesByParent) => {
        return items.reduce((entries, item) => {
          const currItems = entries.get(parent);
          if (currItems !== undefined) {
            const newItems = currItems.filter((_) => _.name !== item.name);
            entries.set(parent, newItems);
          }
          if (item.isDir) {
            entries.delete(stripPrefix(`${parent}/${item.name}`, "/"));
          }
          return entries;
        }, new Map(entriesByParent));
      });
    }

    /** @type {(parent: string, items: readonly FileListItem[]) => Promise<void>} */
    async function delDirItems(parent, items) {
      await items.reduce(async (resP, item) => {
        await resP;

        if (item.isDir) {
          const dir = stripPrefix(`${parent}/${item.name}`, "/");
          const fileListDir = await self.readDir(`${self.rootPath}/${dir}`);
          if (fileListDir.items.length > 0) {
            return await delDirItems(dir, fileListDir.items);
          }

          deleteFromState(parent, [item]);
        }
      }, Promise.resolve());

      const paths = items.map((item) => {
        const name = item.isDir ? `${item.name}/` : item.name;
        return stripPrefix(`${parent}/${name}`, "/");
      });

      const subProcessP = SubProcess.wrap(
        SubProcess.spawn("zip", ["-qd", self.zipPath, ...paths], {
          windowsHide: true,
        }),
      );

      deleteFromState(parent, items);
      const s = await subProcessP;
      s.stdout.readable.destroy();
      const error = await s.exitP;
      if (error) {
        throw error;
      }
    }

    const parentPath = stripPrefix(stripPrefix(parent, self.rootPath), "/");
    return delDirItems(parentPath, items);
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
