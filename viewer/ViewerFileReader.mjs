/**
 * @typedef {import("./ViewerFileLine.mjs").ViewerFileLine} ViewerFileLine
 */
import Encoding from "../file/Encoding.mjs";
import FileReader from "../file/FileReader.mjs";
import ViewerFileLine from "./ViewerFileLine.mjs";

class ViewerFileReader {
  /**
   * @param {FileReader} fileReader
   * @param {number} [bufferSize]
   * @param {number} [maxLineLength]
   */
  constructor(fileReader, bufferSize = 64 * 1024, maxLineLength = 1024) {
    /** @private @readonly @type {FileReader} */
    this.fileReader = fileReader;

    /** @private @readonly @type {number} */
    this.bufferSize = bufferSize;

    /** @private @readonly @type {number} */
    this.maxLineLength = maxLineLength;

    /** @private @readonly @type {Buffer} */
    this.fileBuf = Buffer.allocUnsafe(Math.max(bufferSize, maxLineLength));
  }

  /**
   * @param {string} filePath
   * @returns {Promise<void>}
   */
  open(filePath) {
    return this.fileReader.open(filePath);
  }

  /**
   * @returns {Promise<void>}
   */
  close() {
    return this.fileReader.close();
  }

  /**
   * @param {number} lines
   * @param {number} position
   * @param {number} maxPos
   * @param {string} encoding
   * @returns {Promise<ViewerFileLine[]>}
   */
  readPrevLines(lines, position, maxPos, encoding) {
    /** @type {ViewerFileLine[]} */
    const res = [];
    /** @type {Buffer} */
    let leftBuf = Buffer.from([]);
    const bufSize = lines > 1 ? this.bufferSize : this.maxLineLength;
    const maxLineLength = this.maxLineLength;
    const fileReader = this.fileReader;
    const fileBuf = this.fileBuf;

    /** @type {(buf: Buffer, fromEnd: boolean) => void} */
    function loopOverBuffer(buf, fromEnd) {
      while (true) {
        const suffix =
          buf.length > maxLineLength
            ? buf.subarray(buf.length - maxLineLength, buf.length)
            : buf;

        const rightNewLineIdx = (() => {
          if (fromEnd) return suffix.length;
          else {
            const idx = suffix.lastIndexOf("\n".charCodeAt(0), suffix.length);
            return idx >= 0 && idx < suffix.length - 1 ? suffix.length : idx;
          }
        })();

        const leftNewLineIdx =
          rightNewLineIdx <= 0
            ? -1
            : suffix.lastIndexOf("\n".charCodeAt(0), rightNewLineIdx - 1);

        if (leftNewLineIdx < 0 && buf.length < maxLineLength) {
          leftBuf = Buffer.from(buf);
          return;
        }

        const line = (() => {
          if (rightNewLineIdx < 0) {
            return ViewerFileLine(
              Encoding.decode(suffix, encoding, 0, suffix.length),
              suffix.length,
            );
          }
          if (leftNewLineIdx < 0) {
            return ViewerFileLine(
              Encoding.decode(suffix, encoding, 0, rightNewLineIdx),
              suffix.length,
            );
          }
          return ViewerFileLine(
            Encoding.decode(
              suffix,
              encoding,
              leftNewLineIdx + 1,
              rightNewLineIdx,
            ),
            suffix.length - leftNewLineIdx - 1,
          );
        })();
        res.unshift(line);

        if (res.length < lines && line.bytes < buf.length) {
          buf = buf.subarray(0, buf.length - line.bytes);
          fromEnd = false;
        } else return;
      }
    }

    /** @type {(position: number) => Promise<ViewerFileLine[]>} */
    function loop(position) {
      const [from, size] =
        position > bufSize
          ? [position - bufSize, bufSize]
          : [0, Math.trunc(position)];

      return fileReader
        .readBytes(from, fileBuf.subarray(0, size))
        .then((bytesRead) => {
          const buf = fileBuf.subarray(0, bytesRead);
          const resBuf = Buffer.concat(
            [buf, leftBuf],
            buf.length + leftBuf.length,
          );
          leftBuf = Buffer.from([]);

          loopOverBuffer(resBuf, position === maxPos);

          if (res.length < lines && from > 0) {
            return loop(from);
          }

          if (res.length < lines && leftBuf.length > 0) {
            const line = ViewerFileLine(
              Encoding.decode(leftBuf, encoding, 0, leftBuf.length).trim(),
              leftBuf.length,
            );
            res.unshift(line);
          }

          return Promise.resolve(res);
        });
    }

    return position === 0.0 ? Promise.resolve([]) : loop(position);
  }

  /**
   * @param {number} lines
   * @param {number} position
   * @param {string} encoding
   * @returns {Promise<ViewerFileLine[]>}
   */
  readNextLines(lines, position, encoding) {
    /** @type {ViewerFileLine[]} */
    const res = [];
    /** @type {Buffer} */
    let leftBuf = Buffer.from([]);
    const bufSize = lines > 1 ? this.bufferSize : this.maxLineLength;
    const maxLineLength = this.maxLineLength;
    const fileReader = this.fileReader;
    const fileBuf = this.fileBuf;

    /** @type {(buf: Buffer) => void} */
    function loopOverBuffer(buf) {
      while (true) {
        const prefix = buf.subarray(0, maxLineLength);

        const newLineIndex = prefix.indexOf("\n".charCodeAt(0), 0);
        if (newLineIndex < 0 && buf.length < maxLineLength) {
          leftBuf = Buffer.from(buf);
          return;
        }

        const line = (() => {
          if (newLineIndex < 0) {
            return ViewerFileLine(
              Encoding.decode(prefix, encoding, 0, prefix.length),
              prefix.length,
            );
          }
          return ViewerFileLine(
            Encoding.decode(prefix, encoding, 0, newLineIndex),
            newLineIndex + 1,
          );
        })();
        res.push(line);

        if (res.length < lines && line.bytes < buf.length) {
          buf = buf.subarray(line.bytes, buf.length);
        } else return;
      }
    }

    /** @type {(position: number) => Promise<ViewerFileLine[]>} */
    function loop(position) {
      return fileReader
        .readBytes(position, fileBuf.subarray(0, bufSize))
        .then((bytesRead) => {
          const buf = fileBuf.subarray(0, bytesRead);
          const resBuf = Buffer.concat(
            [leftBuf, buf],
            leftBuf.length + buf.length,
          );
          leftBuf = Buffer.from([]);

          if (resBuf.length > 0) {
            loopOverBuffer(resBuf);
          }

          if (res.length < lines && buf.length > 0) {
            return loop(position + buf.length);
          }

          if (res.length < lines && leftBuf.length > 0) {
            const line = ViewerFileLine(
              Encoding.decode(leftBuf, encoding, 0, leftBuf.length),
              leftBuf.length,
            );
            res.push(line);
          }

          return Promise.resolve(res);
        });
    }

    return loop(position);
  }
}

export default ViewerFileReader;
