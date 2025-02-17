import React from "react";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FileViewHistoryPopup from "./FileViewHistoryPopup.mjs";
import FileEvent from "../FileEvent.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showPopup: boolean;
 *  onClose(): void;
 * }} FileViewHistoryControllerProps
 */

/**
 * @param {FileViewHistoryControllerProps} props
 */
const FileViewHistoryController = (props) => {
  const { fileViewHistoryPopup } = FileViewHistoryController;

  const stacks = WithStacks.useStacks();

  return props.showPopup
    ? h(fileViewHistoryPopup, {
        onAction: (history) => {
          props.onClose();

          WithStacksProps.active(stacks).input.emit("keypress", undefined, {
            name: "",
            full: FileEvent.onFileView,
            data: history,
          });
        },
        onClose: props.onClose,
      })
    : null;
};

FileViewHistoryController.displayName = "FileViewHistoryController";
FileViewHistoryController.fileViewHistoryPopup = FileViewHistoryPopup;

export default FileViewHistoryController;
