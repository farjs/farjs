/**
 * @typedef {import("@farjs/filelist/stack/WithStacks.mjs").WithStacksProps} WithStacksProps
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("@farjs/filelist/FileListData.mjs").FileListData} FileListData
 * @typedef {import("./FileListUi.mjs").FileListUiData} FileListUiData
 */
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileListUi from "./FileListUi.mjs";

class FileListUiPluginImpl extends FileListPlugin {
  constructor() {
    super(["f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"]);
  }

  /**
   * @param {string} key
   * @param {WithStacksProps} stacks
   * @returns {Promise<ReactComponent | undefined>}
   */
  onKeyTrigger(key, stacks) {
    const data = (() => {
      const stackItem = WithStacksProps.active(stacks).stack.peek();
      return stackItem.getData();
    })();

    const uiData = this._createUiData(key, data);
    const res = uiData ? FileListUi(uiData) : undefined;
    return Promise.resolve(res);
  }

  /**
   * @param {string} key
   * @param {FileListData | undefined} data
   * @returns {FileListUiData | undefined}
   */
  _createUiData(key, data) {
    /**
     * @param {Partial<FileListUiData>} partial
     * @returns {FileListUiData}
     */
    function toFileListUiData(partial) {
      return {
        ...partial,
        onClose: () => {},
        data,
      };
    }

    if (key === "f1") {
      return toFileListUiData({ showHelpPopup: true });
    } else if (
      key === "f7" &&
      data &&
      data.actions.api.capabilities.has(FileListCapability.mkDirs)
    ) {
      return toFileListUiData({ showMkFolderPopup: true });
    } else if (
      (key === "f8" || key === "delete") &&
      data &&
      data.actions.api.capabilities.has(FileListCapability.delete) &&
      (FileListState.selectedItems(data.state).length > 0 ||
        FileListState.currentItem(
          data.state,
          (_) => _.name !== FileListItem.up.name
        ))
    ) {
      return toFileListUiData({ showDeletePopup: true });
    } else if (key === "f9") {
      return toFileListUiData({ showMenuPopup: true });
    } else if (key === "f10") {
      return toFileListUiData({ showExitPopup: true });
    } else if (key === "M-s") {
      return toFileListUiData({ showSelectPopup: true });
    } else if (key === "M-d") {
      return toFileListUiData({ showSelectPopup: false });
    }

    return undefined;
  }
}

const FileListUiPlugin = new FileListUiPluginImpl();

export default FileListUiPlugin;
