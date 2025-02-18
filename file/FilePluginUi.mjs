/**
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 */
import React from "react";
import FileViewHistoryController from "./popups/FileViewHistoryController.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showFileViewHistoryPopup: boolean;
 * }} FilePluginUiParams
 */

/**
 * @param {FilePluginUiParams} params
 */
function FilePluginUi({ showFileViewHistoryPopup }) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const FilePluginUiComp = (props) => {
    const { fileViewHistory } = FilePluginUi;

    return h(
      React.Fragment,
      null,
      h(fileViewHistory, {
        showPopup: showFileViewHistoryPopup,
        onClose: props.onClose,
      })
    );
  };

  FilePluginUiComp.displayName = "FilePluginUi";

  return FilePluginUiComp;
}

FilePluginUi.fileViewHistory = FileViewHistoryController;

export default FilePluginUi;
