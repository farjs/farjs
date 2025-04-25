import React from "react";
import FolderShortcutsPopup from "./FolderShortcutsPopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showPopup: boolean;
 *  onChangeDir(dir: string): void;
 *  onClose(): void;
 * }} FolderShortcutsControllerProps
 */

/**
 * @param {FolderShortcutsControllerProps} props
 */
const FolderShortcutsController = (props) => {
  const { folderShortcutsPopup } = FolderShortcutsController;

  return props.showPopup
    ? h(folderShortcutsPopup, {
        onChangeDir: (dir) => {
          props.onClose();
          props.onChangeDir(dir);
        },
        onClose: props.onClose,
      })
    : null;
};

FolderShortcutsController.displayName = "FolderShortcutsController";
FolderShortcutsController.folderShortcutsPopup = FolderShortcutsPopup;

export default FolderShortcutsController;
