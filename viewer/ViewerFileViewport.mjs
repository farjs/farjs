/**
 * @typedef {import("./ViewerFileLine.mjs").ViewerFileLine} ViewerFileLine
 */
import UiString from "@farjs/ui/UiString.mjs";
import Encoding from "../file/Encoding.mjs";
import ViewerFileLine from "./ViewerFileLine.mjs";
import ViewerFileReader from "./ViewerFileReader.mjs";

/**
 * @typedef {{
 *  readonly encoding: string;
 *  readonly size: number;
 *  readonly width: number;
 *  readonly height: number;
 *  readonly wrap: boolean;
 *  readonly column: number;
 *  readonly position: number;
 *  readonly linesData: readonly ViewerFileLine[];
 * }} ViewerFileViewportData
 */

/**
 * @typedef {ViewerFileViewportData & {
 *  readonly fileReader: ViewerFileReader;
 *  readonly content: string;
 *  readonly scrollIndicators: readonly number[];
 *  readonly progress: number;
 *  moveUp(lines: number, from?: number): Promise<ViewerFileViewport>;
 *  moveDown(lines: number): Promise<ViewerFileViewport>;
 *  reload(from?: number): Promise<ViewerFileViewport>;
 *  updated(data: Partial<ViewerFileViewportData>): ViewerFileViewport;
 * }} ViewerFileViewport
 */

/**
 * @implements {ViewerFileViewport}
 */
class ViewerFileViewportImpl {
  /**
   * @param {ViewerFileReader} fileReader
   * @param {string} encoding
   * @param {number} size
   * @param {number} width
   * @param {number} height
   * @param {boolean} wrap
   * @param {number} column
   * @param {number} position
   * @param {readonly ViewerFileLine[]} linesData
   */
  constructor(
    fileReader,
    encoding,
    size,
    width,
    height,
    wrap,
    column,
    position,
    linesData,
  ) {
    /** @readonly @type {ViewerFileReader} */ this.fileReader = fileReader;
    /** @readonly @type {string} */ this.encoding = encoding;
    /** @readonly @type {number} */ this.size = size;
    /** @readonly @type {number} */ this.width = width;
    /** @readonly @type {number} */ this.height = height;
    /** @readonly @type {boolean} */ this.wrap = wrap;
    /** @readonly @type {number} */ this.column = column;
    /** @readonly @type {number} */ this.position = position;
    /** @readonly @type {readonly ViewerFileLine[]} */ this.linesData =
      linesData;

    /** @private @type {string | undefined} */ this._content = undefined;
    /** @private @type {readonly number[] | undefined} */ this._scrollIndicators =
      undefined;
    /** @private @type {number | undefined} */ this._progress = undefined;
  }

  /**
   * @returns {string}
   */
  get content() {
    if (this._content === undefined) {
      /** @type {string[]} */
      const buf = [];
      this.linesData.forEach((line) => {
        const lineStr = UiString(line.line).slice(
          this.column,
          this.column + this.width,
        );
        const chars = Array.from(lineStr);
        for (let j = 0; j < chars.length; j += 1) {
          const str = chars[j];
          const ch = str.charCodeAt(0);
          const isControl =
            ch === 0x00 || // nul
            ch === 0x07 || // bel
            ch === 0x08 || // backspace
            ch === 0x0b || // vertical tab
            ch === 0x1b || // ESC
            ch === 0x7f; // <DEL>

          chars[j] =
            isControl && str !== "\t" && str !== "\r" && str !== "\n"
              ? " "
              : str;
        }
        buf.push(chars.join(""), "\n");
      });

      this._content = buf.join("");
    }

    return this._content;
  }

  /**
   * @returns {readonly number[]}
   */
  get scrollIndicators() {
    if (this._scrollIndicators === undefined) {
      const maybeIndexes = this.linesData.map((line, index) => {
        return UiString(line.line).strWidth() > this.column + this.width
          ? index
          : undefined;
      });
      this._scrollIndicators = maybeIndexes.filter((_) => _ !== undefined);
    }

    return this._scrollIndicators;
  }

  /**
   * @returns {number}
   */
  get progress() {
    if (this._progress === undefined) {
      const bytes = this.linesData.reduce((res, line) => res + line.bytes, 0);
      const viewed = this.position + bytes;
      this._progress =
        this.size === 0.0 ? 0 : Math.trunc((viewed / this.size) * 100);
    }

    return this._progress;
  }

  /**
   * @param {number} lines
   * @param {number} [from]
   * @returns {Promise<ViewerFileViewport>}
   */
  async moveUp(lines, from = this.position) {
    if (from === 0.0) {
      return this;
    }

    const data = await this.fileReader
      .readPrevLines(lines, from, this.size, this.encoding)
      .then(this._doWrap(lines, true));

    if (data.length > 0) {
      const bytes = data.reduce((sum, _) => sum + _.bytes, 0);
      if (from < this.size) {
        data.push(...this.linesData);
      }

      return this.updated({
        position: Math.max(from - bytes, 0),
        linesData: data.slice(0, Math.min(this.height, data.length)),
      });
    }

    return this;
  }

  /**
   * @param {number} lines
   * @returns {Promise<ViewerFileViewport>}
   */
  async moveDown(lines) {
    const bytes = this.linesData.reduce((sum, _) => sum + _.bytes, 0);
    const nextPosition = this.position + bytes;
    if (nextPosition >= this.size) {
      return this.linesData.length === this.height
        ? this.moveUp(this.height, this.size)
        : Promise.resolve(this);
    }

    const data = await this.fileReader
      .readNextLines(lines, nextPosition, this.encoding)
      .then(this._doWrap(lines, false));

    if (data.length > 0) {
      const bytes = this.linesData
        .slice(0, Math.min(lines, this.linesData.length))
        .reduce((sum, _) => sum + _.bytes, 0);

      const res = this.linesData.slice(Math.min(lines, this.linesData.length));
      res.push(...data);

      return this.updated({
        position: this.position + bytes,
        linesData: res,
      });
    }

    return this;
  }

  /**
   * @param {number} [from]
   * @returns {Promise<ViewerFileViewport>}
   */
  reload(from = this.position) {
    return this.fileReader
      .readNextLines(this.height, from, this.encoding)
      .then(this._doWrap(this.height, false))
      .then((linesData) => {
        return this.updated({
          position: from,
          linesData,
        });
      });
  }

  /**
   * @param {number} lines
   * @param {boolean} up
   * @returns {(data: ViewerFileLine[]) => ViewerFileLine[]}
   */
  _doWrap(lines, up) {
    const encoding = this.encoding;
    const width = this.width;

    return (data) => {
      if (!this.wrap) {
        return data;
      }

      /** @type {ViewerFileLine[]} */
      const res = [];

      /** @type {(fileLine: ViewerFileLine) => void} */
      function loop(fileLine) {
        while (true) {
          const { line, bytes } = fileLine;
          if (line.length <= width) {
            res.push(fileLine);
            return;
          }

          const [wrapped, rest] = (() => {
            if (up) {
              return [
                //takeRight
                line.slice(
                  Math.min(line.length - Math.max(width, 0), line.length),
                  line.length,
                ),
                //dropRight
                line.slice(
                  0,
                  Math.min(line.length - Math.max(width, 0), line.length),
                ),
              ];
            }
            return [
              //take
              line.slice(0, Math.min(width, line.length)),
              //drop
              line.slice(Math.min(width, line.length), line.length),
            ];
          })();

          const wrappedBytes = Encoding.byteLength(wrapped, encoding);
          res.push(ViewerFileLine(wrapped, wrappedBytes));

          fileLine = ViewerFileLine(rest, Math.max(bytes - wrappedBytes, 0));
        }
      }

      if (up) {
        data.reduceRight((_, d) => loop(d), /** @type {void} */ (undefined));
        const r = res.reverse();
        //takeRight
        return r.slice(
          Math.min(r.length - Math.max(lines, 0), r.length),
          r.length,
        );
      }

      data.reduce((_, d) => loop(d), /** @type {void} */ (undefined));
      //take
      return res.slice(0, Math.min(lines, res.length));
    };
  }

  /**
   * @param {Partial<ViewerFileViewportData>} data
   * @returns {ViewerFileViewport}
   */
  updated({
    encoding,
    size,
    width,
    height,
    wrap,
    column,
    position,
    linesData,
  }) {
    return new ViewerFileViewportImpl(
      this.fileReader,
      encoding ?? this.encoding,
      size ?? this.size,
      width ?? this.width,
      height ?? this.height,
      wrap ?? this.wrap,
      column ?? this.column,
      position ?? this.position,
      linesData ?? this.linesData,
    );
  }
}

/**
 * @param {ViewerFileReader} fileReader
 * @param {string} encoding
 * @param {number} size
 * @param {number} width
 * @param {number} height
 * @param {boolean} [wrap]
 * @param {number} [column]
 * @param {number} [position]
 * @param {readonly ViewerFileLine[]} [linesData]
 * @returns {ViewerFileViewport}
 */
export function createViewerFileViewport(
  fileReader,
  encoding,
  size,
  width,
  height,
  wrap = false,
  column = 0,
  position = 0,
  linesData = [],
) {
  return new ViewerFileViewportImpl(
    fileReader,
    encoding,
    size,
    width,
    height,
    wrap,
    column,
    position,
    linesData,
  );
}
