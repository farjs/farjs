import ViewerFileReader from "./ViewerFileReader.mjs";

/**
 * @typedef {{
 *  open?: ViewerFileReader['open'];
 *  close?: ViewerFileReader['close'];
 *  readPrevLines?: ViewerFileReader['readPrevLines'];
 *  readNextLines?: ViewerFileReader['readNextLines'];
 * }} ViewerFileReaderMocks
 */

class MockViewerFileReader extends ViewerFileReader {
  /**
   * @param {ViewerFileReaderMocks} mocks
   */
  constructor({ open, close, readPrevLines, readNextLines } = {}) {
    super(/** @type {any} */ (undefined));

    this.open = open ?? this.open;
    this.close = close ?? this.close;
    this.readPrevLines = readPrevLines ?? this.readPrevLines;
    this.readNextLines = readNextLines ?? this.readNextLines;
  }

  /** @type {ViewerFileReader['open']} */
  open() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {ViewerFileReader['close']} */
  close() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {ViewerFileReader['readPrevLines']} */
  readPrevLines() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {ViewerFileReader['readNextLines']} */
  readNextLines() {
    return Promise.reject(new Error("Not implemented!"));
  }
}

export default MockViewerFileReader;
