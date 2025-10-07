/**
 * @import { FileSource, FileTarget } from "@farjs/filelist/api/FileListApi.mjs"
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @typedef {import("./FSService.mjs").FSService} FSService
 */
import path from "path";
import nodeFs from "fs";
import fsPromises from "fs/promises";
import FileListApi from "@farjs/filelist/api/FileListApi.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import FSService from "./FSService.mjs";

/**
 * @typedef {{
 *  lstatSync(path: string): nodeFs.Stats;
 *  rmdirSync(path: string): void;
 *  unlinkSync(path: string): void;
 *  mkdir(path: string): Promise<void>;
 *  readdir(path: string): Promise<readonly string[]>;
 *  open(path: string, flags: number): Promise<fsPromises.FileHandle>;
 * }} FS
 */

class FSFileListApi extends FileListApi {
  /**
   * @param {FS} fs
   * @param {FSService} fsService
   */
  constructor(
    fs = { ...nodeFs, ...fsPromises },
    fsService = FSService.instance
  ) {
    super(
      true,
      new Set([
        FileListCapability.read,
        FileListCapability.write,
        FileListCapability.delete,
        FileListCapability.mkDirs,
        FileListCapability.copyInplace,
        FileListCapability.moveInplace,
      ])
    );

    /** @private @readonly @type {FS} */
    this.fs = fs;

    /** @private @readonly @type {FSService} */
    this.fsService = fsService;
  }

  /** @type {FileListApi['readDir']} */
  async readDir(parent, dir) {
    const targetDir = path.resolve(parent, dir ?? "");
    const files = await this.fs.readdir(targetDir);
    const items = files.map((name) => {
      return this.readFileListItem(targetDir, name);
    });

    return {
      path: targetDir,
      isRoot: FSFileListApi._isRoot(targetDir),
      items,
    };
  }

  /** @type {FileListApi['delete']} */
  delete(parent, items) {
    const self = this;

    /** @type {(parent: string, items: [string, boolean][]) => Promise<void>} */
    function loop(parent, items) {
      return items.reduce(async (resP, [name, isDir]) => {
        await resP;
        if (isDir) {
          const dir = path.join(parent, name);
          const files = await fsPromises.readdir(dir);
          /** @type {[string, boolean][]} */
          const items = files.map((name) => {
            const stats = self.fs.lstatSync(path.join(dir, name));
            return [name, stats.isDirectory()];
          });

          await loop(dir, items);
          self.fs.rmdirSync(dir);
        } else {
          self.fs.unlinkSync(path.join(parent, name));
        }
      }, Promise.resolve());
    }

    return loop(
      parent,
      items.map((i) => [i.name, i.isDir])
    );
  }

  /** @type {FileListApi['mkDirs']} */
  mkDirs(dirs) {
    const self = this;

    /** @type {(parent: string, names: readonly string[]) => Promise<string>} */
    async function loop(parent, names) {
      if (names.length === 0) {
        return parent;
      }

      const [name, ...tail] = names;
      const dir = await (async () => {
        if (name.length === 0) {
          return parent;
        }

        const dir = path.join(parent, name);
        if (parent.length > 0 || !FSFileListApi._isRoot(dir)) {
          await self.fs.mkdir(dir).catch((error) => {
            if (error.code === "EEXIST") {
              //skip
            } else throw error;
          });
        }
        return dir;
      })();

      return loop(dir, tail);
    }

    return loop("", dirs);
  }

  /** @type {FileListApi['readFile']} */
  async readFile(parent, file, position) {
    const filePath = path.join(parent, file.name);
    const handle = await this.fs.open(filePath, FSConstants.O_RDONLY);
    let pos = position;

    /** @type {FileSource} */
    const source = {
      file: filePath,

      readNextBytes: async (buff) => {
        const { bytesRead } = await handle.read(buff, 0, buff.length, pos);
        pos += bytesRead;
        return bytesRead;
      },

      close: handle.close,
    };
    return source;
  }

  /** @type {FileListApi['writeFile']} */
  async writeFile(parent, fileName, onExists) {
    const self = this;
    const filePath = path.join(parent, fileName);

    /** @type {[fsPromises.FileHandle | undefined, number]} */
    const [handle, position] = await this.fs
      .open(
        filePath,
        FSConstants.O_CREAT | FSConstants.O_WRONLY | FSConstants.O_EXCL
      )
      .then(
        (handle) => [handle, 0],
        async (error) => {
          if (error.code === "EEXIST") {
            const existing = self.readFileListItem(parent, fileName);
            const overwrite = await onExists(existing);
            if (overwrite === undefined) {
              return [undefined, 0];
            }
            const handle = await self.fs.open(filePath, FSConstants.O_WRONLY);
            const position = overwrite ? 0 : existing.size;
            return [handle, position];
          }

          throw error;
        }
      );

    if (handle) {
      let pos = position;

      /** @type {FileTarget} */
      const target = {
        file: filePath,

        async writeNextBytes(buff, length) {
          const { bytesWritten } = await handle.write(buff, 0, length, pos);
          if (bytesWritten !== length) {
            throw Error(
              `File write error: bytesWritten(${bytesWritten}) != expected(${length}), file: ${filePath}`
            );
          }
          pos += bytesWritten;
          return pos;
        },

        async setAttributes(src) {
          await handle.truncate(pos);
          await handle.utimes(src.atimeMs / 1000, src.mtimeMs / 1000);
        },

        close: handle.close,

        delete: async () => self.fs.unlinkSync(filePath),
      };
      return target;
    }

    return undefined;
  }

  /** @type {FileListApi['getDriveRoot']} */
  async getDriveRoot(path) {
    const disk = await this.fsService.readDisk(path);
    return disk?.root;
  }

  /** @private @type {(targetDir: string, name: string) => FileListItem} */
  readFileListItem(targetDir, name) {
    try {
      const stats = this.fs.lstatSync(path.join(targetDir, name));
      return FSFileListApi._toFileListItem(name, stats);
    } catch (_) {
      return FileListItem(name);
    }
  }
}

/** @type {(name: string, stats: nodeFs.Stats) => FileListItem} */
FSFileListApi._toFileListItem = (name, stats) => {
  const isDir = stats.isDirectory();
  return {
    ...FileListItem(name, isDir),
    isSymLink: stats.isSymbolicLink(),
    size: isDir ? 0 : stats.size,
    atimeMs: stats.atimeMs,
    mtimeMs: stats.mtimeMs,
    ctimeMs: stats.ctimeMs,
    birthtimeMs: stats.birthtimeMs,
    permissions: FSFileListApi._getPermissions(stats.mode),
  };
};

const FSConstants = nodeFs.constants;
const S_IFDIR = FSConstants.S_IFDIR;
const S_IRUSR = FSConstants.S_IRUSR ?? 0;
const S_IWUSR = FSConstants.S_IWUSR ?? 0;
const S_IXUSR = FSConstants.S_IXUSR ?? 0;
const S_IRGRP = FSConstants.S_IRGRP ?? 0;
const S_IWGRP = FSConstants.S_IWGRP ?? 0;
const S_IXGRP = FSConstants.S_IXGRP ?? 0;
const S_IROTH = FSConstants.S_IROTH ?? 0;
const S_IWOTH = FSConstants.S_IWOTH ?? 0;
const S_IXOTH = FSConstants.S_IXOTH ?? 0;

/** @type {(dir: string) => boolean} */
FSFileListApi._isRoot = (dir) => {
  const pathObj = path.parse(dir);
  return pathObj.root === pathObj.dir && (pathObj.base ?? "").length === 0;
};

/** @type {(mode: number) => string} */
FSFileListApi._getPermissions = (mode) => {
  /** @type {(c: string, f: number) => string} */
  function flag(c, f) {
    return (mode & f) !== 0 ? c : "-";
  }

  const chars = [
    flag("d", S_IFDIR),
    flag("r", S_IRUSR),
    flag("w", S_IWUSR),
    flag("x", S_IXUSR),
    flag("r", S_IRGRP),
    flag("w", S_IWGRP),
    flag("x", S_IXGRP),
    flag("r", S_IROTH),
    flag("w", S_IWOTH),
    flag("x", S_IXOTH),
  ];

  return chars.join("");
};

export default FSFileListApi;
