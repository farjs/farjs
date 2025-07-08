import React from "react";
import FoldersHistoryPopup from "./FoldersHistoryPopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showPopup: boolean;
 *  onChangeDir(dir: string): void;
 *  onClose(): void;
 * }} FoldersHistoryControllerProps
 */

/**
 * @param {FoldersHistoryControllerProps} props
 */
const FoldersHistoryController = (props) => {
  const { foldersHistoryPopup } = FoldersHistoryController;

  if (props.showPopup) {
    return h(foldersHistoryPopup, {
      onChangeDir: (dir) => {
        props.onClose();
        props.onChangeDir(dir);
      },
      onClose: props.onClose,
    });
  }

  return null;
};

FoldersHistoryController.displayName = "FoldersHistoryController";
FoldersHistoryController.foldersHistoryPopup = FoldersHistoryPopup;

export default FoldersHistoryController;
