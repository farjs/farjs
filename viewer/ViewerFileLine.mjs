/**
 * @typedef {{
 *  readonly line: string;
 *  readonly bytes: number;
 * }} ViewerFileLine
 */

/**
 * @param {string} line
 * @param {number} bytes
 * @returns {ViewerFileLine}
 */
function ViewerFileLine(line, bytes) {
  return {
    line,
    bytes,
  };
}

export default ViewerFileLine;
