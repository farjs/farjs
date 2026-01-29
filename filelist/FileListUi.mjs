/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").FileListData} FileListData
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 */
import React from "react";
import HelpController from "./popups/HelpController.mjs";
import ExitController from "./popups/ExitController.mjs";
import MenuController from "./popups/MenuController.mjs";
import DeleteController from "./popups/DeleteController.mjs";
import MakeFolderController from "./popups/MakeFolderController.mjs";
import SelectController from "./popups/SelectController.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  onClose(): void;
 *  readonly data?: FileListData;
 *  readonly showHelpPopup?: boolean;
 *  readonly showExitPopup?: boolean;
 *  readonly showMenuPopup?: boolean;
 *  readonly showDeletePopup?: boolean;
 *  readonly showMkFolderPopup?: boolean;
 *  readonly showSelectPopup?: boolean;
 * }} FileListUiData
 */

/**
 * @param {FileListUiData} data
 */
function FileListUi(data) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const FileListUiComp = (props) => {
    const {
      helpController,
      exitController,
      menuController,
      deleteController,
      makeFolderController,
      selectController,
    } = FileListUi;

    const uiData = { ...data, onClose: props.onClose };

    return h(
      React.Fragment,
      null,
      h(helpController, uiData),
      h(exitController, uiData),
      h(menuController, uiData),
      h(deleteController, uiData),
      h(makeFolderController, uiData),
      h(selectController, uiData),
    );
  };

  FileListUiComp.displayName = "FileListUi";

  return FileListUiComp;
}

FileListUi.helpController = HelpController;
FileListUi.exitController = ExitController;
FileListUi.menuController = MenuController;
FileListUi.deleteController = DeleteController;
FileListUi.makeFolderController = MakeFolderController;
FileListUi.selectController = SelectController;

export default FileListUi;
