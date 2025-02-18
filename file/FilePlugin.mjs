/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("./FilePluginUi.mjs").FilePluginUiParams} FilePluginUiParams
 */
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FilePluginUi from "./FilePluginUi.mjs";

class FilePluginImpl extends FileListPlugin {
  constructor() {
    super(["M-v"]);
  }

  /**
   * @param {string} key
   * @returns {Promise<ReactComponent | undefined>}
   */
  onKeyTrigger(key) {
    const uiParams = this._createUiParams(key);
    const res = uiParams ? FilePluginUi(uiParams) : undefined;
    return Promise.resolve(res);
  }

  /**
   * @param {string} key
   * @returns {FilePluginUiParams | undefined}
   */
  _createUiParams(key) {
    if (key === "M-v") {
      return {
        showFileViewHistoryPopup: true,
      };
    }

    return undefined;
  }
}

const FilePlugin = new FilePluginImpl();

export default FilePlugin;
