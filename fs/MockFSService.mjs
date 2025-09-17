/**
 * @import { FSService } from "./FSService.mjs"
 */

/**
 * @typedef {{
 *  openItem?: FSService['openItem'];
 *  readDisk?: FSService['readDisk'];
 *  readDisks?: FSService['readDisks'];
 * }} FSServiceMocks
 */

/**
 * @implements {FSService}
 */
class MockFSService {
  /**
   * @param {FSServiceMocks} mocks
   */
  constructor({ openItem, readDisk, readDisks } = {}) {
    this.openItem = openItem ?? this.openItem;
    this.readDisk = readDisk ?? this.readDisk;
    this.readDisks = readDisks ?? this.readDisks;
  }

  /** @type {FSService['openItem']} */
  openItem() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FSService['readDisk']} */
  readDisk() {
    return Promise.reject(new Error("Not implemented!"));
  }

  /** @type {FSService['readDisks']} */
  readDisks() {
    return Promise.reject(new Error("Not implemented!"));
  }
}

export default MockFSService;
