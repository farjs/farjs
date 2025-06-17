/**
 * @typedef {"onViewerOpenLeft"
 *  | "onViewerOpenRight"
 * } ViewerEvent
 */

/** @type {ViewerEvent} */
const onViewerOpenLeft = "onViewerOpenLeft";

/** @type {ViewerEvent} */
const onViewerOpenRight = "onViewerOpenRight";

const ViewerEvent = Object.freeze({
  onViewerOpenLeft,
  onViewerOpenRight,
});

export default ViewerEvent;
