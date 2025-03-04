import FileReader from "./FileReader.mjs";

/**
 * @typedef {{
 *  open?: FileReader['open'];
 *  close?: FileReader['close'];
 *  readBytes?: FileReader['readBytes'];
 * }} FileReaderMocks
 */

class MockFileReader extends FileReader {
  /**
   * @param {FileReaderMocks} mocks
   */
  constructor({ open, close, readBytes } = {}) {
    super();

    this.open = open ?? this.open;
    this.close = close ?? this.close;
    this.readBytes = readBytes ?? this.readBytes;
  }

  /** @type {FileReader['open']} */
  open() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FileReader['close']} */
  close() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FileReader['readBytes']} */
  readBytes() {
    return Promise.reject(new Error("Not implemented!"));
  }
}

export default MockFileReader;
