import nodeFs from "fs/promises";

/**
 * @typedef {{
 *  open(path: string, flags: number): Promise<nodeFs.FileHandle>;
 * }} FS
 */

class FileReader {
  /**
   * @param {FS} fs
   */
  constructor(fs = nodeFs) {
    /** @private @readonly @type {FS} */
    this.fs = fs;

    /** @private @type {nodeFs.FileHandle | undefined} */
    this.handle = undefined;
  }

  /**
   * @param {string} filePath
   * @returns {Promise<void>}
   */
  async open(filePath) {
    this.handle = await this.fs.open(filePath, nodeFs.constants.O_RDONLY);
  }

  /**
   * @returns {Promise<void>}
   */
  async close() {
    await ensureHandle(this.handle)
      .close()
      .catch((err) => {
        console.error(`Failed to close file, error: ${err}`);
      });
  }

  /**
   * @param {number} position
   * @param {Buffer} buf
   * @returns {Promise<number>}
   */
  async readBytes(position, buf) {
    return await ensureHandle(this.handle)
      .read(buf, 0, buf.length, position)
      .then((_) => _.bytesRead)
      .catch((err) => {
        console.error(`Failed to read from file, error: ${err}`);
        throw err;
      });
  }
}

/**
 * @param {nodeFs.FileHandle | undefined} handle
 * @returns {nodeFs.FileHandle}
 */
function ensureHandle(handle) {
  if (!handle) {
    throw Error("File is not open!");
  }
  return handle;
}

export default FileReader;
