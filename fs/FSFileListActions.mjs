/**
 * @import FileListApi from "@farjs/filelist/api/FileListApi.mjs"
 */
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import FSFileListApi from "./FSFileListApi.mjs";

/**
 *
 * @param {FileListApi} api
 * @returns {FileListActions}
 */
function FSFileListActions(api = new FSFileListApi()) {
  return new FileListActions(api);
}

/** @type {FileListActions} */
FSFileListActions.instance = FSFileListActions();

export default FSFileListActions;
